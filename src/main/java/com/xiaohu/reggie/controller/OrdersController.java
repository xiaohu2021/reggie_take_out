package com.xiaohu.reggie.controller;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xiaohu.reggie.common.BaseContext;
import com.xiaohu.reggie.common.R;
import com.xiaohu.reggie.dto.OrdersDto;
import com.xiaohu.reggie.entity.AddressBook;
import com.xiaohu.reggie.entity.OrderDetail;
import com.xiaohu.reggie.entity.Orders;
import com.xiaohu.reggie.entity.User;
import com.xiaohu.reggie.service.AddressBookService;
import com.xiaohu.reggie.service.OrderDetailService;
import com.xiaohu.reggie.service.OrderService;
import com.xiaohu.reggie.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
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

    @Autowired
    private UserService userService;

    @Autowired
    private AddressBookService addressBookService;

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


    @GetMapping("/page")
    public R<Page> page(int page, int pageSize, String number, LocalDateTime beginTime, LocalDateTime endTime) {
        log.info("beginTime:{},endTime:{}", beginTime, endTime);
        LocalDateTime localDateTimeBegin = null;
        LocalDateTime localDateTimeEnd = null;
        // 对时间参数 进行处理
        if (beginTime != null && endTime != null) {
            // beginTime处理
            Instant instant = beginTime.toInstant(ZoneOffset.UTC);
            ZoneId zoneId = ZoneId.systemDefault();
            localDateTimeBegin = instant.atZone(zoneId).toLocalDateTime();

            // endTime 处理
            Instant instant1 = endTime.toInstant(ZoneOffset.UTC);
            ZoneId zoneId1 = ZoneId.systemDefault();
            localDateTimeEnd = instant1.atZone(zoneId1).toLocalDateTime();

        }

        Page<Orders> pageInfo = new Page<>();
        Page<OrdersDto> ordersDtoPage = new Page<>();

        LambdaQueryWrapper<Orders> lqw = new LambdaQueryWrapper<>();
        //处理 number订单号
        if (!StrUtil.isBlankIfStr(number)) {
            lqw.eq(Orders::getNumber, number);
        }
        if (!StrUtil.isBlankIfStr(localDateTimeBegin)) {
            lqw.ge(Orders::getOrderTime, localDateTimeBegin);
        }
        if (!StrUtil.isBlankIfStr(localDateTimeEnd)) {
            lqw.le(Orders::getOrderTime, localDateTimeEnd);
        }
        lqw.orderByDesc(Orders::getOrderTime);


        orderService.page(pageInfo);
        BeanUtils.copyProperties(pageInfo, ordersDtoPage, "records");
        List<Orders> records = pageInfo.getRecords();

        List<OrdersDto> ordersDtoList = records.stream().map((res) -> {
            OrdersDto ordersDto = new OrdersDto();

            BeanUtils.copyProperties(res, ordersDto);

//            LambdaQueryWrapper<User> userLambdaQueryWrapper = new LambdaQueryWrapper<>();
//            userLambdaQueryWrapper.eq(User::getId, res.getUserId());
//            User one = userService.getOne(userLambdaQueryWrapper);
//            ordersDto.setUserName(one.getName());
            // 通过订单id 查询订单详细信息
            LambdaQueryWrapper<OrderDetail> detailLambdaQueryWrapper = new LambdaQueryWrapper<>();
            detailLambdaQueryWrapper.eq(OrderDetail::getOrderId,res.getId());
            List<OrderDetail> orderDetails = orderDetailService.list(detailLambdaQueryWrapper);
            ordersDto.setOrderDetails(orderDetails);
            //根据userId 查询用户姓名
            Long userId = res.getUserId();
            User user = userService.getById(userId);
            ordersDto.setUserName(user.getName());
            ordersDto.setPhone(user.getPhone());
            // 获取地址信息
            Long addressBookId = res.getAddressBookId();
            AddressBook addressBook = addressBookService.getById(addressBookId);
            ordersDto.setAddress(addressBook.getDetail());
            ordersDto.setConsignee(addressBook.getConsignee());
            return ordersDto;
        }).collect(Collectors.toList());
        ordersDtoPage.setRecords(ordersDtoList);
        return R.success(ordersDtoPage);
    }

    @PutMapping
    public R<String> complete(@RequestBody Orders orders) {
        log.info("订单信息:{}", orders);
        LambdaUpdateWrapper<Orders> luw = new LambdaUpdateWrapper<>();
        luw.set(Orders::getStatus, orders.getStatus()).in(Orders::getId, orders.getId());
        orderService.update(luw);
        return R.success("订单已审核通过");
    }
}
