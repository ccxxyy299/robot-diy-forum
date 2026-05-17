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
    NO_PERMISSION(403, "无操作权限"),
    CATEGORY_NOT_FOUND(400, "分类不存在"),
    CATEGORY_NAME_DUPLICATE(400, "同级别下分类名称已存在"),
    CATEGORY_HAS_CHILDREN(400, "该分类下有子分类，无法删除"),
    CATEGORY_MAX_DEPTH(400, "分类最多支持两级，父分类必须是顶级分类"),
    PARENT_ID_NULL(400, "父分类ID不能为空"),
    CATEGORY_PARENT_SELF(400, "父分类不能是该分类自身"),
    TAG_NOT_FOUND(400, "标签不存在"),
    TAG_NAME_EXISTS(400, "标签名称已存在"),
    INVALID_STATUS(400, "状态值只能是0或1"),
    FILE_EMPTY(400, "上传文件不能为空"),
    FILE_TOO_LARGE(400, "文件大小不能超过5MB"),
    FILE_TYPE_INVALID(400, "仅支持上传图片文件"),
    FILE_UPLOAD_FAILED(500, "文件上传失败"),
    SERVER_ERROR(500, "服务器内部错误");

    private final Integer code;

    private final String message;
}
