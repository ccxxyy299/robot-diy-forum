package com.slz.demo.server.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.slz.demo.pojo.entity.ForumTopic;
import org.apache.ibatis.annotations.Mapper;

/**
 * 主题帖 Mapper
 */
@Mapper
public interface ForumTopicMapper extends BaseMapper<ForumTopic> {
}