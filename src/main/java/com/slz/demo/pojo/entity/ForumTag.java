package com.slz.demo.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableName;
import com.tangzc.mpe.autotable.annotation.Column;
import com.tangzc.mpe.autotable.annotation.ColumnId;
import com.tangzc.mpe.autotable.annotation.Table;
import com.tangzc.mpe.autotable.annotation.UniqueIndex;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 论坛标签实体
 */
@Data
@TableName("forum_tag")
@Table(value = "forum_tag", comment = "标签表")
public class ForumTag {

    @ColumnId(mode = IdType.AUTO, type = "bigint", comment = "标签ID")
    private Long id;

    @UniqueIndex(name = "uk_forum_tag_name", comment = "标签名称唯一")
    @Column(type = "varchar", length = 50, notNull = true, comment = "标签名称")
    private String name;

    @Column(type = "bigint", notNull = true, comment = "创建者ID")
    private Long creatorId;

    @Column(value = "create_time", type = "datetime", notNull = true, defaultValue = "CURRENT_TIMESTAMP", comment = "创建时间")
    private LocalDateTime createTime;

    @Column(value = "update_time", type = "datetime", comment = "更新时间")
    private LocalDateTime updateTime;

    @Column(type = "tinyint", notNull = true, defaultValue = "0", comment = "逻辑删除: 0未删除 1已删除")
    private Integer isDeleted;
}
