package com.slz.demo.server.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
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
import com.slz.demo.pojo.dto.UserPageQueryDTO;
import com.slz.demo.pojo.entity.User;
import com.slz.demo.pojo.vo.UserVO;
import com.slz.demo.server.mapper.UserMapper;
import com.slz.demo.server.service.MinioService;
import com.slz.demo.server.service.UserService;
import com.slz.demo.server.config.MinioConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.unit.DataSize;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;

/**
 * 用户 Service 实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    private final JwtUtil jwtUtil;

    private final PasswordUtil passwordUtil;

    private final MinioService minioService;

    private final MinioConfig minioConfig;

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
        if (user == null || !passwordUtil.matches(dto.getPassword(), user.getPassword())) {
            throw new BusinessException(ErrorCode.LOGIN_FAILED);
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
            vo.setAvatar(minioService.getPresignedUrl(user.getAvatar()));
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
            // 校验文件大小
            if (avatar.getSize() > DataSize.parse(minioConfig.getMaxImageSize()).toBytes()) {
                throw new BusinessException(ErrorCode.PARAM_ERROR, "头像大小超过限制");
            }
            // 上传到 MinIO
            String extension = FileUtil.getExtension(avatar.getOriginalFilename());
            String objectName = "avatar/" + id + "_" + System.currentTimeMillis()
                    + (extension.isEmpty() ? "" : "." + extension);
            minioService.upload(avatar, objectName);
            newAvatarPath = objectName;
        }

        try {
            if (newAvatarPath != null) {
                user.setAvatar(newAvatarPath);
            }
            if (nickname != null && !nickname.isBlank()) {
                user.setNickname(nickname);
            }
            if (email != null && !email.isBlank()) {
                if (lambdaQuery().eq(User::getEmail, email).ne(User::getId, id).exists()) {
                    throw new BusinessException(ErrorCode.EMAIL_REGISTERED);
                }
                user.setEmail(email);
            }
            user.setUpdateTime(LocalDateTime.now());
            updateById(user);
        } catch (Exception e) {
            // 回滚：删除新上传的头像
            if (newAvatarPath != null) {
                try {
                    minioService.delete(newAvatarPath);
                } catch (Exception ex) {
                    log.warn("回滚头像删除失败: {}", newAvatarPath, ex);
                }
            }
            if (e instanceof BusinessException businessException) {
                throw businessException;
            }
            throw new BusinessException(ErrorCode.FILE_UPLOAD_FAILED);
        }

        // 删除旧头像
        if (newAvatarPath != null && oldAvatarPath != null) {
            try {
                minioService.delete(oldAvatarPath);
            } catch (Exception e) {
                log.warn("旧头像删除失败: {}", oldAvatarPath, e);
            }
        }
    }

    @Override
    public void updateUserStatus(Long targetUserId, boolean status) {
        User targetUser = getById(targetUserId);
        if (targetUser == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }

        if (targetUser.getRole() == UserRole.ADMIN) {
            throw new BusinessException(ErrorCode.CANNOT_MODIFY_ADMIN);
        }

        targetUser.setStatus(status ? 1 : 0);
        targetUser.setUpdateTime(LocalDateTime.now());
        updateById(targetUser);
    }

    @Override
    public Page<UserVO> page(UserPageQueryDTO queryDTO) {
        Page<User> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(queryDTO.getUsername() != null && !queryDTO.getUsername().isBlank(),
                        User::getUsername, queryDTO.getUsername())
                .like(queryDTO.getEmail() != null && !queryDTO.getEmail().isBlank(),
                        User::getEmail, queryDTO.getEmail())
                .eq(queryDTO.getStatus() != null, User::getStatus, queryDTO.getStatus())
                .ge(queryDTO.getCreateTimeStart() != null, User::getCreateTime, queryDTO.getCreateTimeStart())
                .le(queryDTO.getCreateTimeEnd() != null, User::getCreateTime, queryDTO.getCreateTimeEnd())
                .orderByDesc(User::getCreateTime);

        Page<User> userPage = page(page, wrapper);

        // 转换为VO分页
        Page<UserVO> voPage = new Page<>(userPage.getCurrent(), userPage.getSize(), userPage.getTotal());
        voPage.setRecords(userPage.getRecords().stream().map(user -> {
            UserVO vo = new UserVO();
            BeanUtils.copyProperties(user, vo);
            if (user.getAvatar() != null) {
                vo.setAvatar(minioService.getPresignedUrl(user.getAvatar()));
            }
            return vo;
        }).toList());
        return voPage;
    }
}
