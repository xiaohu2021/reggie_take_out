package com.xiaohu.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiaohu.reggie.common.CustomException;
import com.xiaohu.reggie.entity.Category;
import com.xiaohu.reggie.entity.Dish;
import com.xiaohu.reggie.entity.Setmeal;
import com.xiaohu.reggie.mapper.CategoryMapper;
import com.xiaohu.reggie.service.CategoryService;
import com.xiaohu.reggie.service.DishService;
import com.xiaohu.reggie.service.SetmealService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {


    @Autowired
    private DishService dishService;
    @Autowired
    private SetmealService setmealService;

    /**
     * 根据id删除分类，删除之前要进行判断
     * @param id
     */
    @Override
    public void remove(Long id) {
        // 构造查询条件
        LambdaQueryWrapper<Dish> lqw = new LambdaQueryWrapper<Dish>();
        // 添加查询条件，根据分类id进行查询
        lqw.eq(Dish::getCategoryId,id);
        int count = dishService.count(lqw);

        // 查询当前分类是否关联了菜品，如果已经关联，则抛出一个异常
        if(count > 0){
            // 已经关联菜品，抛出一个异常
            throw new CustomException("当前分类下关联了菜品，不能删除");
        }

        // 查询当前分类是否关联了套餐，如果已经关联，则抛出一个异常
        LambdaQueryWrapper<Setmeal> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(Setmeal::getCategoryId,id);
        int set_count = setmealService.count(lambdaQueryWrapper);

        if (set_count > 0){
            // 已经关联套餐，抛出一个异常
            throw new CustomException("当前分类下关联了套餐，不能删除");
        }
        // 正常删除分类
        super.removeById(id);
    }
}
