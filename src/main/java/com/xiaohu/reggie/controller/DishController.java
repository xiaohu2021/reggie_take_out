package com.xiaohu.reggie.controller;

import com.xiaohu.reggie.entity.DishFlavor;
import com.xiaohu.reggie.service.DishFlavorService;
import com.xiaohu.reggie.service.DishService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/dish")
public class DishController {

    @Autowired
    private DishService dishService;
    
    @Autowired
    private DishFlavorService dishFlavorService;


}
