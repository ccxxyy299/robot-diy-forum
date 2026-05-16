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
 * 论坛附件实体
 */
@Data
@TableName("forum_attachment")
@Table(value = "forum_attachment", comment = "附件表")
public class ForumAttachment {

    @ColumnId(mode = IdType.AUTO, type = "bigint", comment = "附件ID")
    private Long id;

    @Column(type = "varchar", length = 255, notNull = true, comment = "文件名")
    private String fileName;

    @Column(type = "varchar", length = 500, notNull = true, comment = "文件路径")
    private String filePath;

    @Column(type = "bigint", notNull = true, comment = "文件大小，单位字节")
    private Long fileSize;

    @Column(type = "varchar", length = 20, notNull = true, comment = "文件类型: IMAGE/FILE")
    private String fileType;

    @Column(type = "varchar", length = 20, notNull = true, comment = "关联类型: TOPIC/REPLY")
    private String relatedType;

    @Column(type = "bigint", notNull = true, comment = "关联业务ID")
    private Long relatedId;

    @Index(name = "idx_forum_attachment_uploader_id", type = IndexTypeEnum.NORMAL, comment = "上传人ID索引")
    @Column(type = "bigint", notNull = true, comment = "上传人ID")
    private Long uploaderId;

    @Column(value = "create_time", type = "datetime", notNull = true, defaultValue = "CURRENT_TIMESTAMP", comment = "创建时间")
    private LocalDateTime createTime;

    @Column(value = "update_time", type = "datetime", comment = "更新时间")
    private LocalDateTime updateTime;

    @Column(type = "tinyint", notNull = true, defaultValue = "0", comment = "逻辑删除: 0未删除 1已删除")
    private Integer isDeleted;
}
