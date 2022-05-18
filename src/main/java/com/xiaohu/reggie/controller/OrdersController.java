package com.xiaohu.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sun.org.apache.xpath.internal.operations.Or;
import com.xiaohu.reggie.common.BaseContext;
import com.xiaohu.reggie.common.R;
import com.xiaohu.reggie.dto.OrdersDto;
import com.xiaohu.reggie.entity.OrderDetail;
import com.xiaohu.reggie.entity.Orders;
import com.xiaohu.reggie.service.OrderDetailService;
import com.xiaohu.reggie.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@Slf4j
@RequestMapping("/order")
public class OrdersController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderDetailService orderDetailService;

    @PostMapping("/submit")
    public R<String> submit(@RequestBody Orders orders) {
        log.info("订单数据:{}", orders);
        orderService.submit(orders);
        return R.success("提交成功");
    }

    @GetMapping("/userPage")
    public R<Page> userPage(int page, int pageSize) {
        log.info("分页数据:{},{}", page, pageSize);
        Page<Orders> pageInfo = new Page<>(page, pageSize);
        Page<OrdersDto> ordersDtoPage = new Page<>();

        LambdaQueryWrapper<Orders> lqw = new LambdaQueryWrapper<>();
        lqw.eq(Orders::getUserId, BaseContext.getCurrentId());
        lqw.orderByAsc(Orders::getOrderTime);
        orderService.page(pageInfo, lqw);

        BeanUtils.copyProperties(pageInfo, ordersDtoPage, "records");
        List<Orders> records = pageInfo.getRecords();
        List<OrdersDto> collect = records.stream().map((res) -> {
            OrdersDto ordersDto = new OrdersDto();
            BeanUtils.copyProperties(res, ordersDto);
            String number = res.getNumber();// 订单id
            LambdaQueryWrapper<OrderDetail> lambdaQueryWrapper = new LambdaQueryWrapper<>();
            lambdaQueryWrapper.eq(OrderDetail::getOrderId, number);
            List<OrderDetail> list = orderDetailService.list(lambdaQueryWrapper);
            ordersDto.setOrderDetails(list);
            return ordersDto;
        }).collect(Collectors.toList());
        ordersDtoPage.setRecords(collect);
        return R.success(ordersDtoPage);
    }
}
