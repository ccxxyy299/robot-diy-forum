package com.slz.demo.server.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.slz.demo.pojo.dto.TopicDTO;
import com.slz.demo.pojo.dto.TopicQueryDTO;
import com.slz.demo.pojo.entity.ForumTopic;
import com.slz.demo.pojo.vo.TopicVO;

import java.util.List;

/**
 * 主题帖 Service
 */
public interface ForumTopicService extends IService<ForumTopic> {

    /**
     * 新增主题帖
     * @param dto 主题帖信息
     */
    void add(TopicDTO dto);

    /**
     * 删除主题帖
     * @param id 主题帖ID
     */
    void delete(Long id);

    /**
     * 修改主题帖
     * @param id 主题帖ID
     * @param dto 主题帖信息
     */
    void update(Long id, TopicDTO dto);

    /**
     * 查询所有主题帖
     * @return 所有主题帖
     */
    List<TopicVO> selectAll();

    /**
     * 分页查询主题帖
     * @param queryDTO 查询条件
     * @return 分页查询结果
     */
    Page<TopicVO> page(TopicQueryDTO queryDTO);
}