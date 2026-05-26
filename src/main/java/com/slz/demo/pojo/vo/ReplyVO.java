package com.slz.demo.pojo.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 回复返回
 */
@Data
public class ReplyVO {

    private Long id;

    private Long topicId;

    private Long creatorId;

    private String creatorNickname;

    private Long parentReplyId;

    private Long replyToUserId;

    private String replyToUserNickname;

    private String content;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    private Integer isDeleted;

    /**
     * 附件列表
     */
    private List<AttachmentVO> attachments;
}