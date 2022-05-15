package com.xiaohu.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiaohu.reggie.common.CustomException;
import com.xiaohu.reggie.dto.SetmealDto;
import com.xiaohu.reggie.entity.Setmeal;
import com.xiaohu.reggie.entity.SetmealDish;
import com.xiaohu.reggie.mapper.SetmealMapper;
import com.xiaohu.reggie.service.SetmealDishService;
import com.xiaohu.reggie.service.SetmealService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class SetmealServiceImpl extends ServiceImpl<SetmealMapper, Setmeal> implements SetmealService {

    @Autowired
    private SetmealDishService setmealDishService;

    @Override
    @Transactional
    public void saveSetmealWithDish(SetmealDto setmealDto) {
        // 保存套餐中的数据
        this.save(setmealDto);
        Long setmealId = setmealDto.getId();
        // 保存套餐中选定的菜品
        List<SetmealDish> setmealDishes = setmealDto.getSetmealDishes();
        setmealDishes = setmealDishes.stream().map((item) -> {
            item.setSetmealId(setmealId);
            return item;
        }).collect(Collectors.toList());
        setmealDishService.saveBatch(setmealDishes);
    }

    @Override
    @Transactional
    public void removeWithDish(List<Long> ids) {
        // 查询套餐状态，确定是否可以删除 起售
        LambdaQueryWrapper<Setmeal> lqw = new LambdaQueryWrapper<>();
        lqw.in(Setmeal::getId, ids);
        lqw.eq(Setmeal::getStatus, 1);
        int count = this.count(lqw);
        // 如果不能删除 ，则 抛出一个业务异常
        if (count > 0) {
            throw new CustomException("套餐正在售卖中，不能删除");
        }
        // 如果可以删除，先删除套餐表中的数据
        this.removeByIds(ids);
        // 删除关联表中的数据
        LambdaQueryWrapper<SetmealDish> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.in(SetmealDish::getDishId, ids);
        setmealDishService.remove(lambdaQueryWrapper);
    }
}
