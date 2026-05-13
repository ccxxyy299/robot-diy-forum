package com.slz.demo.common.enumeration;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 用户角色枚举
 */
@Getter
@AllArgsConstructor
public enum UserRole {

    USER("USER", "普通用户"),
    ADMIN("ADMIN", "管理员");

    @EnumValue
    private final String value;

    private final String desc;
}
