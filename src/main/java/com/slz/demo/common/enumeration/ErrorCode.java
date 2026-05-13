package com.slz.demo.common.enumeration;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 业务错误码枚举，统一管理异常信息
 */
@Getter
@AllArgsConstructor
public enum ErrorCode {

    USERNAME_EXISTS(400, "用户名已存在"),
    EMAIL_REGISTERED(400, "邮箱已注册"),
    EMAIL_NOT_FOUND(400, "邮箱未注册"),
    PASSWORD_ERROR(400, "密码错误"),
    USER_NOT_FOUND(400, "用户不存在"),
    UNAUTHORIZED(401, "当前用户未登录"),
    TOKEN_INVALID(401, "token无效或已过期"),
    SERVER_ERROR(500, "服务器内部错误");

    private final Integer code;

    private final String message;
}
