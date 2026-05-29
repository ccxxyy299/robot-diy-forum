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
    LOGIN_FAILED(400, "邮箱或密码错误"),
    USER_NOT_FOUND(400, "用户不存在"),
    USER_DISABLED(403, "该账号已被禁用"),
    UNAUTHORIZED(401, "当前用户未登录"),
    TOKEN_INVALID(401, "token无效或已过期"),
    NO_PERMISSION(403, "无操作权限"),
    CANNOT_MODIFY_SELF(400, "不能修改自己的状态"),
    CANNOT_MODIFY_ADMIN(400, "不能修改管理员的状态"),
    CATEGORY_NOT_FOUND(400, "分类不存在"),
    CATEGORY_NAME_DUPLICATE(400, "同级别下分类名称已存在"),
    CATEGORY_HAS_CHILDREN(400, "该分类下有子分类，无法删除"),
    CATEGORY_HAS_TOPICS(400, "该分类下有主题帖，无法删除"),
    CATEGORY_MAX_DEPTH(400, "分类最多支持两级，父分类必须是顶级分类"),
    PARENT_ID_NULL(400, "父分类ID不能为空"),
    CATEGORY_PARENT_SELF(400, "父分类不能是该分类自身"),
    TAG_NOT_FOUND(400, "标签不存在"),
    TAG_NAME_EXISTS(400, "标签名称已存在"),
    TOPIC_NOT_FOUND(400, "主题帖不存在"),
    REPLY_NOT_FOUND(400, "回复不存在"),
    PARENT_REPLY_MISMATCH(400, "父回复不属于该帖子"),
    INVALID_STATUS(400, "状态值只能是0或1"),
    FILE_EMPTY(400, "上传文件不能为空"),
    FILE_TOO_LARGE(400, "文件大小不能超过5MB"),
    FILE_TYPE_INVALID(400, "不支持该附件类型上传"),
    FILE_UPLOAD_FAILED(500, "文件上传失败"),
    ATTACHMENT_NOT_FOUND(400, "附件不存在"),
    PARAM_ERROR(400, "参数错误"),
    SERVER_ERROR(500, "服务器内部错误");

    private final Integer code;

    private final String message;
}
