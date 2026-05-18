package com.slz.demo.server.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.slz.demo.pojo.entity.ForumReply;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 回复 Mapper
 * 复杂 SQL 定义在 resources/mapper/ForumReplyMapper.xml 中
 */
@Mapper
public interface ForumReplyMapper extends BaseMapper<ForumReply> {

    /**
     * 根据ID查询回复（包括已删除的）
     * @param id ID
     * @return 回复
     */
    ForumReply selectByIdWithDeleted(@Param("id") Long id);

    /**
     * 根据主题帖ID查询顶层回复
     * @return 分页结果
     * @param page 分页参数
     * @param topicId 主题帖ID
     */
    Page<ForumReply> selectTopReplyPage(Page<ForumReply> page, @Param("topicId") Long topicId);

    /**
     * 根据父回复ID查询子回复
     * @param page 分页参数
     * @param parentReplyId 父回复ID
     * @return 分页结果
     */
    Page<ForumReply> selectChildReplyPage(Page<ForumReply> page, @Param("parentReplyId") Long parentReplyId);
}