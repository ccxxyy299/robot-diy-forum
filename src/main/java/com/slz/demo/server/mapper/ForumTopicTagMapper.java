package com.slz.demo.server.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.slz.demo.pojo.entity.ForumTopicTag;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 主题帖标签关联 Mapper
 */
@Mapper
public interface ForumTopicTagMapper extends BaseMapper<ForumTopicTag> {

    /**
     * 查询主题帖关联的标签ID列表
     * @param topicId 主题帖ID
     */
    @Select("SELECT tag_id FROM forum_topic_tag WHERE topic_id = #{topicId}")
    List<Long> selectTagIdsByTopicId(@Param("topicId") Long topicId);

    /**
     * 查询标签关联的主题帖ID列表
     * @param tagId 标签ID
     */
    @Select("SELECT topic_id FROM forum_topic_tag WHERE tag_id = #{tagId}")
    List<Long> selectTopicIdsByTagId(@Param("tagId") Long tagId);

    /**
     * 删除主题帖的所有标签关联
     * @param topicId 主题帖ID
     */
    @Delete("DELETE FROM forum_topic_tag WHERE topic_id = #{topicId}")
    void deleteByTopicId(@Param("topicId") Long topicId);

    /**
     * 删除标签的所有主题帖关联
     * @param tagId 标签ID
     */
    @Delete("DELETE FROM forum_topic_tag WHERE tag_id = #{tagId}")
    void deleteByTagId(@Param("tagId") Long tagId);
}