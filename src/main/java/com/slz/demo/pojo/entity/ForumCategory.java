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
 * 论坛分类实体
 */
@Data
@TableName("forum_category")
@Table(value = "forum_category", comment = "论坛分类表")
public class ForumCategory {

    @ColumnId(mode = IdType.AUTO, type = "bigint", comment = "分类ID")
    private Long id;

    @Index(name = "idx_forum_category_parent_id", type = IndexTypeEnum.NORMAL, comment = "父分类ID索引")
    @Column(type = "bigint", notNull = true, defaultValue = "0", comment = "父分类ID，顶级分类为0")
    private Long parentId;

    @Index(name = "idx_forum_category_name", type = IndexTypeEnum.NORMAL, comment = "分类名称索引")
    @Column(type = "varchar", length = 100, notNull = true, comment = "分类名称")
    private String name;

    @Column(type = "varchar", length = 255, comment = "分类描述")
    private String description;

    @Column(type = "bigint", notNull = true, comment = "创建者ID")
    private Long creatorId;

    @Column(value = "create_time", type = "datetime", notNull = true, defaultValue = "CURRENT_TIMESTAMP", comment = "创建时间")
    private LocalDateTime createTime;

    @Column(value = "update_time", type = "datetime", comment = "更新时间")
    private LocalDateTime updateTime;

    @Column(value = "is_deleted", type = "tinyint", notNull = true, defaultValue = "0", comment = "逻辑删除: 0未删除 1已删除")
    private Integer isDeleted;
}
