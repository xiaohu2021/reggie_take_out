package com.xiaohu.reggie.dto;

import com.xiaohu.reggie.entity.Dish;
import com.xiaohu.reggie.entity.Setmeal;
import com.xiaohu.reggie.entity.SetmealDish;
import lombok.Data;

import java.util.List;

@Data
public class SetmealDto extends Setmeal {

    private List<SetmealDish> setmealDishes;
    private String categoryName;
}
