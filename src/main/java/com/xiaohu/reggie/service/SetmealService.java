package com.xiaohu.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xiaohu.reggie.dto.SetmealDto;
import com.xiaohu.reggie.entity.Setmeal;

import java.util.List;

public interface SetmealService extends IService<Setmeal> {

    void saveSetmealWithDish(SetmealDto setmealDto);

    // 删除套餐 及 套餐管理的数据
    void removeWithDish(List<Long> ids);
}
