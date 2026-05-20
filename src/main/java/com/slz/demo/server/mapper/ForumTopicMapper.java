package com.slz.demo.server.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.slz.demo.pojo.entity.ForumTopic;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 主题帖 Mapper
 */
@Mapper
public interface ForumTopicMapper extends BaseMapper<ForumTopic> {

    /**
     * 增加主题帖的浏览数
     * @param id 主题帖ID
     */
    void incrementViewCount(@Param("id") Long id);

    /**
     * 增加主题帖的回复数
     * @param id 主题帖ID
     */
    void incrementReplyCount(@Param("id") Long id);

    /**
     * 减少主题帖的回复数
     * @param id 主题帖ID
     */
    void decrementReplyCount(@Param("id") Long id);
}