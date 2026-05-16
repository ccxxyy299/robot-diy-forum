package com.slz.demo.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.tangzc.autotable.annotation.Index;
import com.tangzc.autotable.annotation.enums.IndexTypeEnum;
import com.tangzc.mpe.autotable.annotation.Column;
import com.tangzc.mpe.autotable.annotation.ColumnId;
import com.tangzc.mpe.autotable.annotation.Table;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 论坛主题帖实体
 */
@Data
@TableName("forum_topic")
@Table(value = "forum_topic", comment = "主题帖表")
public class ForumTopic {

    @ColumnId(mode = IdType.AUTO, type = "bigint", comment = "主题帖ID")
    private Long id;

    @Index(name = "idx_forum_topic_category_id", type = IndexTypeEnum.NORMAL, comment = "分类ID索引")
    @Column(type = "bigint", notNull = true, comment = "所属分类ID")
    private Long categoryId;

    @Index(name = "idx_forum_topic_creator_id", type = IndexTypeEnum.NORMAL, comment = "发帖人ID索引")
    @Column(type = "bigint", notNull = true, comment = "发帖人用户ID")
    private Long creatorId;

    @Column(type = "varchar", length = 150, notNull = true, comment = "标题")
    private String title;

    @Column(type = "text", notNull = true, comment = "正文内容")
    private String content;

    @Index(name = "idx_forum_topic_status", type = IndexTypeEnum.NORMAL, comment = "状态索引")
    @Column(type = "tinyint", notNull = true, defaultValue = "1", comment = "状态: 0隐藏 1正常")
    private Integer status;

    @Column(type = "int", notNull = true, defaultValue = "0", comment = "浏览量")
    private Integer viewCount;

    @Column(type = "int", notNull = true, defaultValue = "0", comment = "回复数")
    private Integer replyCount;

    @Index(name = "idx_forum_topic_create_time", type = IndexTypeEnum.NORMAL, comment = "创建时间索引")
    @Column(value = "create_time", type = "datetime", notNull = true, comment = "创建时间", defaultValue = "CURRENT_TIMESTAMP")
    private LocalDateTime createTime;

    @Column(value = "update_time", type = "datetime", comment = "更新时间")
    private LocalDateTime updateTime;

    @Column(type = "tinyint", notNull = true, defaultValue = "0", comment = "逻辑删除: 0未删除 1已删除")
    private Integer isDeleted;
}
