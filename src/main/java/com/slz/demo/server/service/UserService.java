package com.slz.demo.server.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.slz.demo.pojo.dto.LoginDTO;
import com.slz.demo.pojo.dto.RegisterDTO;
import com.slz.demo.pojo.entity.User;
import com.slz.demo.pojo.vo.UserVO;

/**
 * 用户 Service
 */
public interface UserService extends IService<User> {

    /**
     * 用户注册
     * @param dto 注册参数
     */
    void register(RegisterDTO dto);

    /**
     * 用户登录
     * @param dto 登录参数
     * @return token
     */
    String login(LoginDTO dto);

    /**
     * 获取当前登录用户信息
     * @return 用户信息
     */
    UserVO getCurrentUser();
}
