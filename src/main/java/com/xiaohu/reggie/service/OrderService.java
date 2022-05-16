package com.xiaohu.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xiaohu.reggie.entity.Orders;


public interface OrderService extends IService<Orders> {

    void submit(Orders orders);
}
