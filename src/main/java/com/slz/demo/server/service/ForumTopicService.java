package com.slz.demo.server.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.slz.demo.pojo.dto.TopicAndAttachmentDTO;
import com.slz.demo.pojo.dto.TopicDTO;
import com.slz.demo.pojo.dto.TopicQueryDTO;
import com.slz.demo.pojo.dto.TopicUpdateAndAttachmentDTO;
import com.slz.demo.pojo.entity.ForumTopic;
import com.slz.demo.pojo.vo.TopicVO;

import java.util.List;

/**
 * 主题帖 Service
 */
public interface ForumTopicService extends IService<ForumTopic> {

    /**
     * 新增主题帖（支持附件）
     * @param dto 主题帖信息 + 附件
     * @return 新增的主题帖ID
     */
    Long add(TopicAndAttachmentDTO dto);

    /**
     * 删除主题帖
     * @param id 主题帖ID
     */
    void delete(Long id);

    /**
     * 修改主题帖（支持附件）
     * @param id 主题帖ID
     * @param dto 主题帖信息 + 附件
     */
    void update(Long id, TopicUpdateAndAttachmentDTO dto);

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

    /**
     * 查询主题帖详情
     * @param id 主题帖ID
     * @return 主题帖详情
     */
    TopicVO detail(Long id);

    /**
     * 修改主题帖状态（显示/隐藏）
     * @param topicId 主题帖ID
     * @param status 目标状态（true显示 false隐藏）
     */
    void updateTopicStatus(Long topicId, boolean status);

    /**
     * 查询当前用户的主题帖
     * @param pageNum 页码
     * @param pageSize 每页条数
     * @return 分页结果
     */
    Page<TopicVO> myTopics(Integer pageNum, Integer pageSize);
}