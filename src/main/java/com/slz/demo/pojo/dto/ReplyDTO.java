package com.slz.demo.pojo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 回复新增请求
 */
@Data
public class ReplyDTO {

    @NotNull(message = "主题帖ID不能为空")
    private Long topicId;

    /**
     * 父回复ID，顶层回复传0或不传
     * 不传或传0：回复主题帖（replyToUserId 自动设为贴主）
     * 传其他值：回复某条评论（replyToUserId 自动设为该评论创建者）
     */
    private Long parentReplyId;

    @NotBlank(message = "回复内容不能为空")
    private String content;
}