package com.slz.demo.pojo.dto;

import lombok.Data;

/**
 * 用户资料修改
 */
@Data
public class UserDTO {

    private Long id;

    private String avatar;

    private String nickname;

    private String email;
}
