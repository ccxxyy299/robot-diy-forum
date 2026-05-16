package com.slz.demo.server.controller;

import com.slz.demo.common.result.Result;
import com.slz.demo.common.util.FileUtil;
import com.slz.demo.pojo.dto.LoginDTO;
import com.slz.demo.pojo.dto.RegisterDTO;
import com.slz.demo.pojo.dto.UserDTO;
import com.slz.demo.pojo.vo.UserVO;
import com.slz.demo.server.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.unit.DataSize;
import org.springframework.web.bind.annotation.GetMapping;
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

    @Value("${upload.path}")
    private String uploadPath;

    @Value("${upload.max-image-size}")
    private DataSize maxImageSize;

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
     * @return
     */
    @PostMapping("/login")
    public Result<String> login(@Valid @RequestBody LoginDTO dto) {
        return Result.success(userService.login(dto));
    }

    /**
     * 获取当前用户信息
     * @return
     */
    @GetMapping("/me")
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
    public Result<String> update(@RequestParam("id") Long id,
                                 @RequestParam(value = "avatar", required = false) MultipartFile avatar,
                                 @RequestParam(value = "nickname", required = false) String nickname,
                                 @RequestParam(value = "email", required = false) String email) {
        UserDTO dto = new UserDTO();
        dto.setId(id);
        dto.setNickname(nickname);
        dto.setEmail(email);
        if (avatar != null && !avatar.isEmpty()) {
            dto.setAvatar(FileUtil.saveImage(avatar, uploadPath, maxImageSize.toBytes()));
        }
        userService.update(dto);
        return Result.success("修改成功");
    }
}
