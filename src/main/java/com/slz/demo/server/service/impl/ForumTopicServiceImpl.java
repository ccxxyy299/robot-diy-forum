package com.slz.demo.server.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.slz.demo.common.enumeration.ErrorCode;
import com.slz.demo.common.exception.BusinessException;
import com.slz.demo.common.util.UserContext;
import com.slz.demo.pojo.dto.TopicDTO;
import com.slz.demo.pojo.dto.TopicQueryDTO;
import com.slz.demo.pojo.entity.ForumCategory;
import com.slz.demo.pojo.entity.ForumTag;
import com.slz.demo.pojo.entity.ForumTopic;
import com.slz.demo.pojo.entity.ForumTopicTag;
import com.slz.demo.pojo.vo.TagVO;
import com.slz.demo.pojo.vo.TopicVO;
import com.slz.demo.server.mapper.ForumTopicMapper;
import com.slz.demo.server.mapper.ForumTopicTagMapper;
import com.slz.demo.server.service.ForumCategoryService;
import com.slz.demo.server.service.ForumTagService;
import com.slz.demo.server.service.ForumTopicService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 主题帖 Service 实现
 */
@Service
@RequiredArgsConstructor
public class ForumTopicServiceImpl extends ServiceImpl<ForumTopicMapper, ForumTopic> implements ForumTopicService {

    private final ForumTopicTagMapper topicTagMapper;
    private final ForumCategoryService categoryService;
    private final ForumTagService tagService;

    @Override
    @Transactional
    public void add(TopicDTO dto) {
        // 验证分类是否存在
        ForumCategory category = categoryService.getById(dto.getCategoryId());
        if (category == null) {
            throw new BusinessException(ErrorCode.CATEGORY_NOT_FOUND);
        }

        // 验证标签是否存在（如果有标签）
        if (dto.getTagIds() != null && !dto.getTagIds().isEmpty()) {
            for (Long tagId : dto.getTagIds()) {
                if (!tagService.lambdaQuery().eq(ForumTag::getId, tagId).exists()) {
                    throw new BusinessException(ErrorCode.TAG_NOT_FOUND);
                }
            }
        }

        // 创建主题帖
        ForumTopic topic = new ForumTopic();
        topic.setCategoryId(dto.getCategoryId());
        topic.setCreatorId(UserContext.get().getUserId());
        topic.setTitle(dto.getTitle());
        topic.setContent(dto.getContent());
        topic.setStatus(1);
        topic.setViewCount(0);
        topic.setReplyCount(0);
        save(topic);

        // 保存标签关联
        if (dto.getTagIds() != null && !dto.getTagIds().isEmpty()) {
            for (Long tagId : dto.getTagIds()) {
                ForumTopicTag topicTag = new ForumTopicTag();
                topicTag.setTopicId(topic.getId());
                topicTag.setTagId(tagId);
                topicTagMapper.insert(topicTag);
            }
        }
    }

    @Override
    @Transactional
    public void delete(Long id) {
        ForumTopic topic = getById(id);
        if (topic == null) {
            throw new BusinessException(ErrorCode.TOPIC_NOT_FOUND);
        }

        // 删除主题帖
        removeById(id);

        // 删除标签关联
        LambdaQueryWrapper<ForumTopicTag> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ForumTopicTag::getTopicId, id);
        topicTagMapper.delete(wrapper);
    }

    @Override
    @Transactional
    public void update(Long id, TopicDTO dto) {
        ForumTopic topic = getById(id);
        if (topic == null) {
            throw new BusinessException(ErrorCode.TOPIC_NOT_FOUND);
        }

        // 验证分类是否存在
        ForumCategory category = categoryService.getById(dto.getCategoryId());
        if (category == null) {
            throw new BusinessException(ErrorCode.CATEGORY_NOT_FOUND);
        }

        // 验证标签是否存在（如果有标签）
        if (dto.getTagIds() != null && !dto.getTagIds().isEmpty()) {
            for (Long tagId : dto.getTagIds()) {
                if (!tagService.lambdaQuery().eq(ForumTag::getId, tagId).exists()) {
                    throw new BusinessException(ErrorCode.TAG_NOT_FOUND);
                }
            }
        }

        // 更新主题帖
        topic.setCategoryId(dto.getCategoryId());
        topic.setTitle(dto.getTitle());
        topic.setContent(dto.getContent());
        topic.setUpdateTime(LocalDateTime.now());
        updateById(topic);

        // 删除旧的标签关联
        LambdaQueryWrapper<ForumTopicTag> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ForumTopicTag::getTopicId, id);
        topicTagMapper.delete(wrapper);

        // 保存新的标签关联
        if (dto.getTagIds() != null && !dto.getTagIds().isEmpty()) {
            for (Long tagId : dto.getTagIds()) {
                ForumTopicTag topicTag = new ForumTopicTag();
                topicTag.setTopicId(id);
                topicTag.setTagId(tagId);
                topicTagMapper.insert(topicTag);
            }
        }
    }

    @Override
    public List<TopicVO> selectAll() {
        List<ForumTopic> list = lambdaQuery()
                .eq(ForumTopic::getStatus, 1)
                .orderByDesc(ForumTopic::getCreateTime)
                .list();
        return list.stream().map(this::toVO).toList();
    }

    @Override
    public Page<TopicVO> page(TopicQueryDTO queryDTO) {
        Page<ForumTopic> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        LambdaQueryWrapper<ForumTopic> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ForumTopic::getStatus, 1);

        // 处理分类条件
        List<Long> categoryIds = resolveCategoryIds(queryDTO);
        if (categoryIds != null && !categoryIds.isEmpty()) {
            wrapper.in(ForumTopic::getCategoryId, categoryIds);
        }

        // 处理标签条件
        if (queryDTO.getTagId() != null) {
            List<Long> topicIds = topicTagMapper.selectTopicIdsByTagId(queryDTO.getTagId());
            if (topicIds == null || topicIds.isEmpty()) {
                // 没有关联该标签的主题帖，返回空页
                return new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
            }
            wrapper.in(ForumTopic::getId, topicIds);
        }

        wrapper.orderByDesc(ForumTopic::getCreateTime);
        Page<ForumTopic> topicPage = page(page, wrapper);

        // 转换为VO分页
        Page<TopicVO> voPage = new Page<>(topicPage.getCurrent(), topicPage.getSize(), topicPage.getTotal());
        voPage.setRecords(topicPage.getRecords().stream().map(this::toVO).toList());
        return voPage;
    }

    /**
     * 解析分类ID列表
     * 如果传了categoryId，直接使用
     * 如果只传了parentId，查询该父分类下所有子分类ID
     */
    private List<Long> resolveCategoryIds(TopicQueryDTO queryDTO) {
        // 如果传了categoryId（子分类），优先使用
        if (queryDTO.getCategoryId() != null) {
            return List.of(queryDTO.getCategoryId());
        }

        // 如果只传了parentId（父分类），查询该父分类下所有子分类
        if (queryDTO.getParentId() != null) {
            List<ForumCategory> children = categoryService.lambdaQuery()
                    .eq(ForumCategory::getParentId, queryDTO.getParentId())
                    .list();
            if (children.isEmpty()) {
                return null;
            }
            return children.stream().map(ForumCategory::getId).toList();
        }

        // 都没传，则查询全部
        return null;
    }

    private TopicVO toVO(ForumTopic entity) {
        TopicVO vo = new TopicVO();
        BeanUtils.copyProperties(entity, vo);

        // 设置分类名称
        ForumCategory category = categoryService.getById(entity.getCategoryId());
        if (category != null) {
            vo.setCategoryName(category.getName());
        }

        // 设置标签列表
        List<Long> tagIds = topicTagMapper.selectTagIdsByTopicId(entity.getId());
        if (tagIds != null && !tagIds.isEmpty()) {
            List<TagVO> tags = new ArrayList<>();
            for (Long tagId : tagIds) {
                ForumTag tag = tagService.getById(tagId);
                if (tag != null) {
                    TagVO tagVO = new TagVO();
                    BeanUtils.copyProperties(tag, tagVO);
                    tags.add(tagVO);
                }
            }
            vo.setTags(tags);
        }

        return vo;
    }
}