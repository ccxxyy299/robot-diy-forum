package com.slz.demo.server.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.slz.demo.common.enumeration.ErrorCode;
import com.slz.demo.common.exception.BusinessException;
import com.slz.demo.common.util.UserContext;
import com.slz.demo.pojo.dto.TopicAndAttachmentDTO;
import com.slz.demo.pojo.dto.TopicDTO;
import com.slz.demo.pojo.dto.TopicQueryDTO;
import com.slz.demo.pojo.dto.TopicUpdateAndAttachmentDTO;
import com.slz.demo.pojo.entity.ForumCategory;
import com.slz.demo.pojo.entity.ForumTag;
import com.slz.demo.pojo.entity.ForumTopic;
import com.slz.demo.pojo.entity.ForumTopicTag;
import com.slz.demo.pojo.entity.User;
import com.slz.demo.pojo.vo.TagVO;
import com.slz.demo.pojo.vo.TopicVO;
import com.slz.demo.server.mapper.ForumTopicMapper;
import com.slz.demo.server.mapper.ForumTopicTagMapper;
import com.slz.demo.server.constant.AttachmentConstants;
import com.slz.demo.server.service.ForumAttachmentService;
import com.slz.demo.server.service.ForumCategoryService;
import com.slz.demo.server.service.ForumPermissionService;
import com.slz.demo.server.service.ForumTagService;
import com.slz.demo.server.service.ForumTopicService;
import com.slz.demo.server.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.slz.demo.pojo.vo.AttachmentVO;

/**
 * 主题帖 Service 实现
 */
@Service
@RequiredArgsConstructor
public class ForumTopicServiceImpl extends ServiceImpl<ForumTopicMapper, ForumTopic> implements ForumTopicService {

    private final ForumTopicTagMapper topicTagMapper;
    private final ForumTopicMapper topicMapper;
    private final ForumCategoryService categoryService;
    private final ForumTagService tagService;
    private final UserService userService;
    private final ForumPermissionService forumPermissionService;
    private final ForumAttachmentService attachmentService;

    @Override
    @Transactional
    public Long add(TopicAndAttachmentDTO dto) {
        TopicDTO topicDTO = dto.getTopic();

        // 验证分类是否存在
        ForumCategory category = categoryService.getById(topicDTO.getCategoryId());
        if (category == null) {
            throw new BusinessException(ErrorCode.CATEGORY_NOT_FOUND);
        }

        // 验证标签是否存在，去重避免重复插入
        validateAndSaveTags(topicDTO);

        // 创建主题帖
        ForumTopic topic = new ForumTopic();
        topic.setCategoryId(topicDTO.getCategoryId());
        topic.setCreatorId(UserContext.get().getUserId());
        topic.setTitle(topicDTO.getTitle());
        topic.setContent(topicDTO.getContent());
        topic.setStatus(1);
        topic.setViewCount(0);
        topic.setReplyCount(0);
        save(topic);

        // 保存标签关联
        saveTagRelations(topic.getId(), topicDTO.getTagIds());

        // 保存附件
        attachmentService.saveAttachments(dto.getAttachments(), AttachmentConstants.RELATED_TYPE_TOPIC, topic.getId());

        return topic.getId();
    }

    @Override
    @Transactional
    public void delete(Long id) {
        ForumTopic topic = getById(id);
        if (topic == null) {
            throw new BusinessException(ErrorCode.TOPIC_NOT_FOUND);
        }

        forumPermissionService.checkCanDeleteTopic(topic);

        // 删除关联附件（磁盘文件+数据库记录）
        attachmentService.deleteByRelated(AttachmentConstants.RELATED_TYPE_TOPIC, id);

        removeById(id);

        // 删除标签关联
        LambdaQueryWrapper<ForumTopicTag> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ForumTopicTag::getTopicId, id);
        topicTagMapper.delete(wrapper);
    }

    @Override
    @Transactional
    public void update(Long id, TopicUpdateAndAttachmentDTO dto) {
        ForumTopic topic = getById(id);
        if (topic == null) {
            throw new BusinessException(ErrorCode.TOPIC_NOT_FOUND);
        }

        forumPermissionService.checkCanUpdateTopic(topic);

        TopicDTO topicDTO = dto.getTopic();
        ForumCategory category = categoryService.getById(topicDTO.getCategoryId());
        if (category == null) {
            throw new BusinessException(ErrorCode.CATEGORY_NOT_FOUND);
        }

        validateTags(topicDTO.getTagIds());

        // 更新主题帖
        topic.setCategoryId(topicDTO.getCategoryId());
        topic.setTitle(topicDTO.getTitle());
        topic.setContent(topicDTO.getContent());
        topic.setUpdateTime(LocalDateTime.now());
        updateById(topic);

        // 删除旧的标签关联并保存新的
        LambdaQueryWrapper<ForumTopicTag> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ForumTopicTag::getTopicId, id);
        topicTagMapper.delete(wrapper);
        saveTagRelations(id, topicDTO.getTagIds());

        // 删除指定附件
        attachmentService.deleteByIds(dto.getDeleteAttachmentIds(), id);

        // 保存新增附件
        attachmentService.saveAttachments(dto.getAttachments(), AttachmentConstants.RELATED_TYPE_TOPIC, id);
    }

    @Override
    public List<TopicVO> selectAll() {
        List<ForumTopic> list = lambdaQuery()
                .eq(ForumTopic::getStatus, 1)
                .orderByDesc(ForumTopic::getCreateTime)
                .list();
        return toVOList(list);
    }

    @Override
    public Page<TopicVO> page(TopicQueryDTO queryDTO) {
        Page<ForumTopic> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        LambdaQueryWrapper<ForumTopic> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ForumTopic::getStatus, 1);

        // 处理分类条件
        List<Long> categoryIds = resolveCategoryIds(queryDTO);
        if (categoryIds != null) {
            if (categoryIds.isEmpty()) {
                // 没有符合条件的分类，返回空页
                return new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
            }
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
        voPage.setRecords(toVOList(topicPage.getRecords()));
        return voPage;
    }

    @Override
    public TopicVO detail(Long id) {
        ForumTopic topic = getById(id);
        if (topic == null || topic.getStatus() == 0) {
            throw new BusinessException(ErrorCode.TOPIC_NOT_FOUND);
        }

        // 增加浏览数
        topicMapper.incrementViewCount(id);

        // 查询分类
        ForumCategory category = categoryService.getById(topic.getCategoryId());

        // 查询关联的标签
        List<Long> tagIds = topicTagMapper.selectTagIdsByTopicId(id);
        List<TagVO> tags = new ArrayList<>();
        Map<Long, ForumTag> tagMap = Map.of();
        if (tagIds != null && !tagIds.isEmpty()) {
            tagMap = tagService.listByIds(tagIds)
                    .stream()
                    .collect(Collectors.toMap(ForumTag::getId, t -> t));

            for (Long tagId : tagIds) {
                ForumTag tag = tagMap.get(tagId);
                if (tag != null) {
                    TagVO tagVO = new TagVO();
                    BeanUtils.copyProperties(tag, tagVO);
                    tags.add(tagVO);
                }
            }
        }

        // 合并所有用户ID，批量查询
        Set<Long> userIds = new java.util.HashSet<>();
        userIds.add(topic.getCreatorId());
        tagMap.values().forEach(tag -> userIds.add(tag.getCreatorId()));
        Map<Long, User> userMap = userIds.isEmpty()
                ? Map.of()
                : userService.listByIds(userIds).stream()
                .collect(Collectors.toMap(User::getId, u -> u));

        // 组装VO
        TopicVO vo = new TopicVO();
        BeanUtils.copyProperties(topic, vo);

        User creator = userMap.get(topic.getCreatorId());
        if (creator != null) {
            vo.setCreatorNickname(creator.getNickname());
        }

        if (category != null) {
            vo.setCategoryName(category.getName());
        }

        for (TagVO tagVO : tags) {
            User tagCreator = userMap.get(tagVO.getCreatorId());
            if (tagCreator != null) {
                tagVO.setCreatorNickname(tagCreator.getNickname());
            }
        }
        vo.setTags(tags);

        // 查询附件列表
        vo.setAttachments(attachmentService.listByRelated(AttachmentConstants.RELATED_TYPE_TOPIC, id));

        return vo;
    }

    @Override
    public void updateTopicStatus(Long topicId, boolean status) {
        ForumTopic topic = getById(topicId);
        if (topic == null) {
            throw new BusinessException(ErrorCode.TOPIC_NOT_FOUND);
        }

        topic.setStatus(status ? 1 : 0);
        topic.setUpdateTime(LocalDateTime.now());
        updateById(topic);
    }

    @Override
    public Page<TopicVO> myTopics(Integer pageNum, Integer pageSize) {
        if (pageNum == null || pageNum < 1) {
            pageNum = 1;
        }
        if (pageSize == null || pageSize < 1 || pageSize > 100) {
            pageSize = 10;
        }

        Long userId = UserContext.get().getUserId();

        Page<ForumTopic> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<ForumTopic> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ForumTopic::getCreatorId, userId)
                .orderByDesc(ForumTopic::getCreateTime);
        Page<ForumTopic> topicPage = page(page, wrapper);

        Page<TopicVO> voPage = new Page<>(topicPage.getCurrent(), topicPage.getSize(), topicPage.getTotal());
        voPage.setRecords(toVOList(topicPage.getRecords()));
        return voPage;
    }

    /**
     * 解析分类ID列表
     * 如果传了categoryId，直接使用
     * 如果只传了parentId，查询该父分类下所有子分类ID
     * 返回 null 表示不限制分类（查询全部）
     * 返回空列表表示没有符合条件的分类（查询结果为空）
     */
    private List<Long> resolveCategoryIds(TopicQueryDTO queryDTO) {
        // 如果传了categoryId（子分类），直接使用
        if (queryDTO.getCategoryId() != null) {
            return List.of(queryDTO.getCategoryId());
        }

        // 如果只传了parentId（父分类），查询该父分类下所有子分类
        if (queryDTO.getParentId() != null) {
            List<ForumCategory> children = categoryService.lambdaQuery()
                    .eq(ForumCategory::getParentId, queryDTO.getParentId())
                    .list();
            if (children.isEmpty()) {
                // 没有子分类，返回空列表（查询结果为空）
                return List.of();
            }
            return children.stream().map(ForumCategory::getId).toList();
        }

        // 都没传，则查询全部（不限制分类）
        return null;
    }

    /**
     * 批量转换为VO，一次性查询所有关联数据避免N+1问题
     */
    private List<TopicVO> toVOList(List<ForumTopic> entities) {
        if (entities.isEmpty()) {
            return List.of();
        }

        // 批量查询主题帖创建者用户
        Set<Long> topicCreatorIds = entities.stream()
                .map(ForumTopic::getCreatorId)
                .collect(Collectors.toSet());

        // 批量查询分类
        Set<Long> categoryIds = entities.stream()
                .map(ForumTopic::getCategoryId)
                .collect(Collectors.toSet());
        Map<Long, ForumCategory> categoryMap = categoryService.listByIds(categoryIds)
                .stream()
                .collect(Collectors.toMap(ForumCategory::getId, c -> c));

        // 批量查询所有主题帖关联的标签ID
        Map<Long, List<Long>> topicTagIdsMap = new java.util.HashMap<>();
        Set<Long> allTagIds = new java.util.HashSet<>();
        for (ForumTopic entity : entities) {
            List<Long> tagIds = topicTagMapper.selectTagIdsByTopicId(entity.getId());
            if (tagIds != null && !tagIds.isEmpty()) {
                topicTagIdsMap.put(entity.getId(), tagIds);
                allTagIds.addAll(tagIds);
            }
        }

        // 批量查询标签实体（如果标签ID为空则跳过查询）
        Map<Long, ForumTag> tagMap = allTagIds.isEmpty()
                ? Map.of()
                : tagService.listByIds(allTagIds).stream()
                .collect(Collectors.toMap(ForumTag::getId, t -> t));

        // 批量查询标签创建者用户
        Set<Long> tagCreatorIds = tagMap.values().stream()
                .map(ForumTag::getCreatorId)
                .collect(Collectors.toSet());

        // 合并所有用户ID并批量查询
        Set<Long> allUserIds = new java.util.HashSet<>();
        allUserIds.addAll(topicCreatorIds);
        allUserIds.addAll(tagCreatorIds);
        Map<Long, User> userMap = allUserIds.isEmpty()
                ? Map.of()
                : userService.listByIds(allUserIds).stream()
                .collect(Collectors.toMap(User::getId, u -> u));

        // 批量查询附件并按主题帖ID分组
        List<Long> topicIds = entities.stream().map(ForumTopic::getId).toList();
        Map<Long, List<AttachmentVO>> topicAttachmentsMap = attachmentService.mapByRelatedIds(
                AttachmentConstants.RELATED_TYPE_TOPIC, topicIds);

        return entities.stream()
                .map(entity -> toVO(entity, userMap, categoryMap, tagMap, topicTagIdsMap, topicAttachmentsMap))
                .toList();
    }

    private TopicVO toVO(ForumTopic entity,
                         Map<Long, User> userMap,
                         Map<Long, ForumCategory> categoryMap,
                         Map<Long, ForumTag> tagMap,
                         Map<Long, List<Long>> topicTagIdsMap,
                         Map<Long, List<AttachmentVO>> topicAttachmentsMap) {
        TopicVO vo = new TopicVO();
        BeanUtils.copyProperties(entity, vo);

        // 设置创建者昵称
        User user = userMap.get(entity.getCreatorId());
        if (user != null) {
            vo.setCreatorNickname(user.getNickname());
        }

        // 设置分类名称
        ForumCategory category = categoryMap.get(entity.getCategoryId());
        if (category != null) {
            vo.setCategoryName(category.getName());
        }

        // 设置标签列表（使用预先查询的标签ID映射）
        List<Long> tagIds = topicTagIdsMap.get(entity.getId());
        if (tagIds != null && !tagIds.isEmpty()) {
            List<TagVO> tags = new ArrayList<>();
            for (Long tagId : tagIds) {
                ForumTag tag = tagMap.get(tagId);
                if (tag != null) {
                    TagVO tagVO = new TagVO();
                    BeanUtils.copyProperties(tag, tagVO);

                    // 设置标签创建者昵称
                    User tagCreator = userMap.get(tag.getCreatorId());
                    if (tagCreator != null) {
                        tagVO.setCreatorNickname(tagCreator.getNickname());
                    }

                    tags.add(tagVO);
                }
            }
            vo.setTags(tags);
        }

        // 设置附件列表
        List<AttachmentVO> attachments = topicAttachmentsMap.get(entity.getId());
        if (attachments != null) {
            vo.setAttachments(attachments);
        }

        return vo;
    }

    // ==================== 附件相关辅助方法 ====================

    /**
     * 校验标签是否存在
     */
    private void validateTags(List<Long> tagIds) {
        if (tagIds == null || tagIds.isEmpty()) {
            return;
        }
        List<Long> distinctTagIds = tagIds.stream().distinct().toList();
        for (Long tagId : distinctTagIds) {
            if (!tagService.lambdaQuery().eq(ForumTag::getId, tagId).exists()) {
                throw new BusinessException(ErrorCode.TAG_NOT_FOUND);
            }
        }
    }

    /**
     * 校验标签并保存关联
     */
    private void validateAndSaveTags(TopicDTO dto) {
        validateTags(dto.getTagIds());
    }

    /**
     * 保存标签关联
     */
    private void saveTagRelations(Long topicId, List<Long> tagIds) {
        if (tagIds == null || tagIds.isEmpty()) {
            return;
        }
        tagIds.stream().distinct().forEach(tagId -> {
            ForumTopicTag topicTag = new ForumTopicTag();
            topicTag.setTopicId(topicId);
            topicTag.setTagId(tagId);
            topicTagMapper.insert(topicTag);
        });
    }
}
