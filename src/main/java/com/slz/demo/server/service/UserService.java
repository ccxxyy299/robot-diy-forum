package com.slz.demo.server.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.slz.demo.pojo.dto.LoginDTO;
import com.slz.demo.pojo.dto.RegisterDTO;
import com.slz.demo.pojo.dto.UserPageQueryDTO;
import com.slz.demo.pojo.entity.User;
import com.slz.demo.pojo.vo.UserVO;
import org.springframework.web.multipart.MultipartFile;

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

    /**
     * 修改用户资料
     * @param id 用户ID
     * @param avatar 新头像文件（可为null表示不修改）
     * @param nickname 新昵称（可为null或空表示不修改）
     * @param email 新邮箱（可为null或空表示不修改）
     */
    void update(Long id, MultipartFile avatar, String nickname, String email);

    /**
     * 修改用户状态（启用/禁用）
     * @param targetUserId 目标用户ID
     * @param status 目标状态（true启用 false禁用）
     */
    void updateUserStatus(Long targetUserId, boolean status);

    /**
     * 管理员分页查询用户
     * @param queryDTO 查询参数
     * @return 分页结果
     */
    Page<UserVO> page(UserPageQueryDTO queryDTO);
}
