package com.xiaohu.reggie.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiaohu.reggie.entity.User;
import com.xiaohu.reggie.mapper.UserMapper;
import com.xiaohu.reggie.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
}
