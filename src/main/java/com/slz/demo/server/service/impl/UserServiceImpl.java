package com.slz.demo.server.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.slz.demo.common.enumeration.ErrorCode;
import com.slz.demo.common.enumeration.UserRole;
import com.slz.demo.common.exception.BusinessException;
import com.slz.demo.common.util.FileUtil;
import com.slz.demo.common.util.JwtUtil;
import com.slz.demo.common.util.PasswordUtil;
import com.slz.demo.common.util.UserContext;
import com.slz.demo.pojo.ao.RoleAO;
import com.slz.demo.pojo.dto.LoginDTO;
import com.slz.demo.pojo.dto.RegisterDTO;
import com.slz.demo.pojo.entity.User;
import com.slz.demo.pojo.vo.UserVO;
import com.slz.demo.server.mapper.UserMapper;
import com.slz.demo.server.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.unit.DataSize;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.time.LocalDateTime;

/**
 * 用户 Service 实现
 */
@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Value("${upload.path}")
    private String uploadPath;

    @Value("${upload.max-image-size}")
    private DataSize maxImageSize;

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
    public void update(Long id, MultipartFile avatar, String nickname, String email) {
        RoleAO ao = UserContext.get();
        if (!id.equals(ao.getUserId())) {
            throw new BusinessException(ErrorCode.NO_PERMISSION);
        }
        User user = getById(id);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }

        String newAvatarPath = null;
        String oldAvatarPath = user.getAvatar();

        if (avatar != null && !avatar.isEmpty()) {
            newAvatarPath = FileUtil.saveImage(avatar, uploadPath, maxImageSize.toBytes());
        }

        try {
            if (newAvatarPath != null) {
                user.setAvatar(newAvatarPath);
            }
            if (nickname != null && !nickname.isEmpty()) {
                user.setNickname(nickname);
            }
            if (email != null && !email.isEmpty()) {
                user.setEmail(email);
            }
            user.setUpdateTime(LocalDateTime.now());
            updateById(user);
        } catch (Exception e) {
            if (newAvatarPath != null) {
                File newFile = new File(uploadPath, newAvatarPath);
                if (newFile.exists()) {
                    newFile.delete();
                }
            }
            if (e instanceof BusinessException) {
                throw (BusinessException) e;
            }
            throw new BusinessException(ErrorCode.FILE_UPLOAD_FAILED);
        }

        if (newAvatarPath != null && oldAvatarPath != null) {
            File oldFile = new File(uploadPath, oldAvatarPath);
            if (oldFile.exists()) {
                oldFile.delete();
            }
        }
    }
}
