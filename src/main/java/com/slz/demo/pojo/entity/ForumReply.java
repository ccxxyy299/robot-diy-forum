package com.slz.demo.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableName;
import com.tangzc.autotable.annotation.Index;
import com.tangzc.autotable.annotation.enums.IndexTypeEnum;
import com.tangzc.mpe.autotable.annotation.Column;
import com.tangzc.mpe.autotable.annotation.ColumnId;
import com.tangzc.mpe.autotable.annotation.Table;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 论坛回复实体
 */
@Data
@TableName("forum_reply")
@Table(value = "forum_reply", comment = "回复表")
public class ForumReply {

    @ColumnId(mode = IdType.AUTO, type = "bigint", comment = "回复ID")
    private Long id;

    @Index(name = "idx_forum_reply_topic_id", type = IndexTypeEnum.NORMAL, comment = "主题帖ID索引")
    @Column(type = "bigint", notNull = true, comment = "所属主题帖ID")
    private Long topicId;

    @Index(name = "idx_forum_reply_creator_id", type = IndexTypeEnum.NORMAL, comment = "回复人ID索引")
    @Column(type = "bigint", notNull = true, comment = "回复人用户ID")
    private Long creatorId;

    @Index(name = "idx_forum_reply_parent_reply_id", type = IndexTypeEnum.NORMAL, comment = "父回复ID索引")
    @Column(type = "bigint", notNull = true, defaultValue = "0", comment = "父回复ID，顶层回复为0")
    private Long parentReplyId;

    @Index(name = "idx_forum_reply_reply_to_user_id", type = IndexTypeEnum.NORMAL, comment = "被回复用户ID索引")
    @Column(type = "bigint", comment = "被回复用户ID")
    private Long replyToUserId;

    @Column(type = "text", notNull = true, comment = "回复内容")
    private String content;

    @Index(name = "idx_forum_reply_create_time", type = IndexTypeEnum.NORMAL, comment = "创建时间索引")
    @Column(value = "create_time", type = "datetime", notNull = true, defaultValue = "CURRENT_TIMESTAMP", comment = "创建时间")
    private LocalDateTime createTime;

    @Column(value = "update_time", type = "datetime", comment = "更新时间")
    private LocalDateTime updateTime;

    @Column(type = "tinyint", notNull = true, defaultValue = "0", comment = "逻辑删除: 0未删除 1已删除")
    private Integer isDeleted;
}
