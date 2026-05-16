package com.slz.demo.server.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.slz.demo.common.enumeration.ErrorCode;
import com.slz.demo.common.enumeration.UserRole;
import com.slz.demo.common.exception.BusinessException;
import com.slz.demo.common.util.JwtUtil;
import com.slz.demo.common.util.PasswordUtil;
import com.slz.demo.common.util.UserContext;
import com.slz.demo.pojo.ao.RoleAO;
import com.slz.demo.pojo.dto.UserDTO;
import com.slz.demo.pojo.dto.LoginDTO;
import com.slz.demo.pojo.dto.RegisterDTO;
import com.slz.demo.pojo.entity.User;
import com.slz.demo.pojo.vo.UserVO;
import com.slz.demo.server.mapper.UserMapper;
import com.slz.demo.server.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 用户 Service 实现
 */
@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    private final JwtUtil jwtUtil;

    private final PasswordUtil passwordUtil;

    @Override
    public void register(RegisterDTO dto) {
        if (lambdaQuery().eq(User::getUsername, dto.getUsername()).exists()) {
            throw new BusinessException(ErrorCode.USERNAME_EXISTS);
        }
        if (lambdaQuery().eq(User::getEmail, dto.getEmail()).exists()) {
            throw new BusinessException(ErrorCode.EMAIL_REGISTERED);
        }

        User user = new User();
        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());
        user.setNickname(dto.getNickname());
        user.setRole(UserRole.USER);
        user.setPassword(passwordUtil.encode(dto.getPassword()));
        save(user);
    }

    @Override
    public String login(LoginDTO dto) {
        User user = lambdaQuery().eq(User::getEmail, dto.getEmail()).one();
        if (user == null) {
            throw new BusinessException(ErrorCode.EMAIL_NOT_FOUND);
        }
        if (!passwordUtil.matches(dto.getPassword(), user.getPassword())) {
            throw new BusinessException(ErrorCode.PASSWORD_ERROR);
        }
        return jwtUtil.generateToken(user.getId(), user.getRole());
    }

    @Override
    public UserVO getCurrentUser() {
        RoleAO ao = UserContext.get();
        User user = getById(ao.getUserId());
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
        UserVO vo = new UserVO();
        BeanUtils.copyProperties(user, vo);
        if (user.getAvatar() != null) {
            vo.setAvatar("/upload/" + user.getAvatar());
        }
        return vo;
    }

    @Override
    public void update(UserDTO dto) {
        RoleAO ao = UserContext.get();
        if (!ao.getUserId().equals(dto.getId())) {
            throw new BusinessException(ErrorCode.NO_PERMISSION);
        }
        User user = getById(dto.getId());
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
        if (dto.getAvatar() != null) {
            user.setAvatar(dto.getAvatar());
        }
        if (dto.getNickname() != null) {
            user.setNickname(dto.getNickname());
        }
        if (dto.getEmail() != null) {
            user.setEmail(dto.getEmail());
        }
        user.setUpdateTime(LocalDateTime.now());
        updateById(user);
    }
}
