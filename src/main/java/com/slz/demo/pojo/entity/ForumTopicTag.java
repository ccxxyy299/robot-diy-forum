package com.slz.demo.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableName;
import com.tangzc.autotable.annotation.Index;
import com.tangzc.autotable.annotation.enums.IndexTypeEnum;
import com.tangzc.mpe.autotable.annotation.Column;
import com.tangzc.mpe.autotable.annotation.ColumnId;
import com.tangzc.mpe.autotable.annotation.Table;
import com.tangzc.mpe.autotable.annotation.UniqueIndex;
import lombok.Data;

/**
 * 主题帖标签关联实体
 */
@Data
@TableName("forum_topic_tag")
@Table(value = "forum_topic_tag", comment = "主题帖标签关联表")
public class ForumTopicTag {

    @ColumnId(mode = IdType.AUTO, type = "bigint", comment = "主键ID")
    private Long id;

    @Index(name = "idx_forum_topic_tag_topic_id", type = IndexTypeEnum.NORMAL, comment = "主题帖ID索引")
    @Column(type = "bigint", notNull = true, comment = "主题帖ID")
    private Long topicId;

    @Index(name = "idx_forum_topic_tag_tag_id", type = IndexTypeEnum.NORMAL, comment = "标签ID索引")
    @Column(type = "bigint", notNull = true, comment = "标签ID")
    private Long tagId;
}
