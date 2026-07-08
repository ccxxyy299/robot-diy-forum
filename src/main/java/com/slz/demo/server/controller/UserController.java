package com.slz.demo.server.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.slz.demo.common.enumeration.UserRole;
import com.slz.demo.common.result.Result;
import com.slz.demo.pojo.dto.LoginDTO;
import com.slz.demo.pojo.dto.RegisterDTO;
import com.slz.demo.pojo.dto.UserPageQueryDTO;
import com.slz.demo.pojo.vo.UserVO;
import com.slz.demo.server.annotation.RoleRequired;
import com.slz.demo.server.service.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * 用户
 */
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Value("${jwt.expire}")
    private long jwtExpire;

    /**
     * 用户注册
     * @param dto
     * @return
     */
    @PostMapping("/register")
    public Result<String> register(@Valid @RequestBody RegisterDTO dto) {
        userService.register(dto);
        return Result.success("注册成功");
    }

    /**
     * 用户登录
     * @param dto
     * @param response
     * @return
     */
    @PostMapping("/login")
    public Result<String> login(@Valid @RequestBody LoginDTO dto, HttpServletResponse response) {
        String token = userService.login(dto);

        Cookie cookie = new Cookie("token", token);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setAttribute("SameSite", "Lax");
        cookie.setMaxAge((int) (jwtExpire / 1000));
        response.addCookie(cookie);

        return Result.success("登录成功");
    }

    /**
     * 用户退出登录
     * @param response
     * @return
     */
    @PostMapping("/logout")
    public Result<String> logout(HttpServletResponse response) {
        Cookie cookie = new Cookie("token", null);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);
        return Result.success("已退出");
    }

    /**
     * 获取当前用户信息
     * @return
     */
    @GetMapping("/me")
    @RoleRequired(value = UserRole.USER, allowDisabled = true)
    public Result<UserVO> me() {
        return Result.success(userService.getCurrentUser());
    }

    /**
     * 修改用户信息
     * @param id
     * @param avatar
     * @param nickname
     * @param email
     * @return
     */
    @PutMapping("/update")
    @RoleRequired(UserRole.USER)
    public Result<String> update(@RequestParam("id") Long id,
                                 @RequestParam(value = "avatar", required = false) MultipartFile avatar,
                                 @RequestParam(value = "nickname", required = false) String nickname,
                                 @RequestParam(value = "email", required = false) String email) {
        userService.update(id, avatar, nickname, email);
        return Result.success("修改成功");
    }

    /**
     * 修改用户状态（启用/禁用）
     * @param id 目标用户ID
     * @param status 目标状态（true启用 false禁用）
     * @return 结果
     */
    @PutMapping("/{id}/status")
    @RoleRequired(UserRole.ADMIN)
    public Result<String> updateUserStatus(@PathVariable Long id, @RequestParam boolean status) {
        userService.updateUserStatus(id, status);
        return Result.success("修改成功");
    }

    /**
     * 管理员分页查询用户
     * @param queryDTO 查询参数
     * @return 分页结果
     */
    @PostMapping("/page")
    @RoleRequired(UserRole.ADMIN)
    public Result<Page<UserVO>> page(@RequestBody UserPageQueryDTO queryDTO) {
        return Result.success(userService.page(queryDTO));
    }
}
