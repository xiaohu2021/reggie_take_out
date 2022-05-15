package com.xiaohu.reggie.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiaohu.reggie.entity.SetmealDish;
import com.xiaohu.reggie.mapper.SetmealDishMapper;
import com.xiaohu.reggie.service.SetmealDishService;
import com.xiaohu.reggie.service.SetmealService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class SetmealDishServiceImpl extends ServiceImpl<SetmealDishMapper, SetmealDish>  implements SetmealDishService {
}
