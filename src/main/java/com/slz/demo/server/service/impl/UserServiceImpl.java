package com.slz.demo.server.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.slz.demo.pojo.entity.User;
import com.slz.demo.server.mapper.UserMapper;
import com.slz.demo.server.service.UserService;
import org.springframework.stereotype.Service;


@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
}
