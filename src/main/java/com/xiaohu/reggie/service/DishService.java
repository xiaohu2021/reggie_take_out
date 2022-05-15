package com.xiaohu.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xiaohu.reggie.dto.DishDto;
import com.xiaohu.reggie.entity.Dish;

import java.util.List;

public interface DishService extends IService<Dish> {

    //新增菜品，同时插入菜品对应的口味数据，需要操作两张表：dish，dish_flavor
    void saveWithFlavor(DishDto dishDto);

    // 根据id查询 dish和dish_flavor表
    DishDto getByIdWithFlavor(Long id);

    // 更新 dish 和 dish_flavor表
    void updateDishWithFlavor(DishDto dishDto);

}
