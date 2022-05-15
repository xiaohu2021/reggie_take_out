package com.xiaohu.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiaohu.reggie.dto.DishDto;
import com.xiaohu.reggie.entity.Dish;
import com.xiaohu.reggie.entity.DishFlavor;
import com.xiaohu.reggie.mapper.DishMapper;
import com.xiaohu.reggie.service.DishFlavorService;
import com.xiaohu.reggie.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class DishServiceImpl extends ServiceImpl<DishMapper, Dish> implements DishService {

    @Autowired
    private DishFlavorService dishFlavorService;



    /**
     * 新增菜品 并且 保存对应的口味数据
     *
     * @param dishDto
     */
    @Override
    @Transactional
    public void saveWithFlavor(DishDto dishDto) {
        // 保存菜品的基本信息到菜品表dish
        this.save(dishDto);
        // 保存菜品口味数据到菜品口味表 dish_flavor
        Long dishId = dishDto.getId(); // 菜品id
        // 菜品口味
        List<DishFlavor> flavors = dishDto.getFlavors();

        flavors = flavors.stream().map((item) -> {
            item.setDishId(dishId);
            return item;
        }).collect(Collectors.toList());
        dishFlavorService.saveBatch(flavors);
    }

    @Override
    public DishDto getByIdWithFlavor(Long id) {
        //查询菜品基本信息 从 dish表查询
        Dish dish = this.getById(id);
        DishDto dishDto = new DishDto();
        // 对象拷贝
        BeanUtils.copyProperties(dish, dishDto);
        // 查询当前菜品对应的口味信息，从 dish_flavor信息
        LambdaQueryWrapper<DishFlavor> lqw = new LambdaQueryWrapper<>();
        lqw.eq(dish.getId() != null, DishFlavor::getDishId, dish.getId());
        List<DishFlavor> flavors = dishFlavorService.list(lqw);
        dishDto.setFlavors(flavors);
        return dishDto;
    }

    @Override
    @Transactional
    public void updateDishWithFlavor(DishDto dishDto) {
        // 更新dish表基本信息
        this.updateById(dishDto);

        // 清理当前菜品对应的口味数据 --dish_flavor表的delete操作
        LambdaQueryWrapper<DishFlavor> lqw = new LambdaQueryWrapper<>();
        lqw.eq(DishFlavor::getDishId,dishDto.getId());
        dishFlavorService.remove(lqw);
        // 添加当前提交过来的口味数据--dish_flavor 的 insert操作。
        // 保存菜品口味数据到菜品口味表 dish_flavor
        Long dishId = dishDto.getId(); // 菜品id
        // 菜品口味
        List<DishFlavor> flavors = dishDto.getFlavors();
        flavors = flavors.stream().map((item) -> {
            item.setDishId(dishId);
            return item;
        }).collect(Collectors.toList());
        dishFlavorService.saveBatch(flavors);
    }

}
