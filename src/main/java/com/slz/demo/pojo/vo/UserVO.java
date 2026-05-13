package com.slz.demo.pojo.vo;

import com.slz.demo.common.enumeration.UserRole;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户信息返回
 */
@Data
public class UserVO {

    private Long id;

    private String username;

    private String email;

    private String avatar;

    private UserRole role;

    private Integer status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
