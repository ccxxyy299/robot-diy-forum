package com.slz.demo.server.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.slz.demo.server.constant.ForumConstants;
import com.slz.demo.common.enumeration.ErrorCode;
import com.slz.demo.common.exception.BusinessException;
import com.slz.demo.common.util.UserContext;
import com.slz.demo.pojo.dto.ReplyDTO;
import com.slz.demo.pojo.dto.ReplyTopQueryDTO;
import com.slz.demo.pojo.dto.ReplyChildQueryDTO;
import com.slz.demo.pojo.entity.ForumReply;
import com.slz.demo.pojo.entity.ForumTopic;
import com.slz.demo.pojo.entity.User;
import com.slz.demo.pojo.vo.ReplyVO;
import com.slz.demo.server.mapper.ForumReplyMapper;
import com.slz.demo.server.service.ForumReplyService;
import com.slz.demo.server.service.ForumTopicService;
import com.slz.demo.server.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 回复 Service 实现
 */
@Service
@RequiredArgsConstructor
public class ForumReplyServiceImpl extends ServiceImpl<ForumReplyMapper, ForumReply> implements ForumReplyService {

    private final ForumTopicService topicService;
    private final UserService userService;

    @Override
    @Transactional
    public void add(ReplyDTO dto) {
        // 验证主题帖是否存在
        ForumTopic topic = topicService.getById(dto.getTopicId());
        if (topic == null) {
            throw new BusinessException(ErrorCode.TOPIC_NOT_FOUND);
        }

        Long parentReplyId = dto.getParentReplyId() != null ? dto.getParentReplyId() : 0L;

        // 确定 replyToUserId
        Long replyToUserId;
        if (parentReplyId == 0L) {
            // 顶层回复：回复主题帖，replyToUserId 为贴主ID
            replyToUserId = topic.getCreatorId();
        } else {
            // 回复某条评论：查询父回复，replyToUserId 为父回复创建者ID
            ForumReply parentReply = getById(parentReplyId);
            if (parentReply == null) {
                throw new BusinessException(ErrorCode.REPLY_NOT_FOUND);
            }
            // 校验父回复是否属于当前帖子
            if (!parentReply.getTopicId().equals(dto.getTopicId())) {
                throw new BusinessException(ErrorCode.PARENT_REPLY_MISMATCH);
            }
            replyToUserId = parentReply.getCreatorId();
        }

        // 创建回复
        ForumReply reply = new ForumReply();
        reply.setTopicId(dto.getTopicId());
        reply.setCreatorId(UserContext.get().getUserId());
        reply.setParentReplyId(parentReplyId);
        reply.setReplyToUserId(replyToUserId);
        reply.setContent(dto.getContent());
        save(reply);

        // 更新主题帖回复数
        topic.setReplyCount(topic.getReplyCount() + 1);
        topicService.updateById(topic);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        ForumReply reply = getById(id);
        if (reply == null) {
            throw new BusinessException(ErrorCode.REPLY_NOT_FOUND);
        }

        removeById(id);

        // 更新主题帖回复数
        ForumTopic topic = topicService.getById(reply.getTopicId());
        if (topic != null && topic.getReplyCount() > 0) {
            topic.setReplyCount(topic.getReplyCount() - 1);
            topicService.updateById(topic);
        }
    }

    @Override
    public Page<ReplyVO> pageTopReply(ReplyTopQueryDTO queryDTO) {
        // 验证主题帖是否存在
        ForumTopic topic = topicService.getById(queryDTO.getTopicId());
        if (topic == null) {
            throw new BusinessException(ErrorCode.TOPIC_NOT_FOUND);
        }

        Page<ForumReply> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        Page<ForumReply> replyPage = baseMapper.selectTopReplyPage(page, queryDTO.getTopicId());

        // 转换为VO分页
        Page<ReplyVO> voPage = new Page<>(replyPage.getCurrent(), replyPage.getSize(), replyPage.getTotal());
        voPage.setRecords(toVOList(replyPage.getRecords()));
        return voPage;
    }

    @Override
    public Page<ReplyVO> pageChildReply(ReplyChildQueryDTO queryDTO) {
        // 验证父回复是否存在（包括已删除的，因为可能显示占位）
        ForumReply parentReply = baseMapper.selectByIdWithDeleted(queryDTO.getParentReplyId());
        if (parentReply == null) {
            throw new BusinessException(ErrorCode.REPLY_NOT_FOUND);
        }

        // 验证父回复所属的主题帖是否存在
        ForumTopic topic = topicService.getById(parentReply.getTopicId());
        if (topic == null) {
            throw new BusinessException(ErrorCode.TOPIC_NOT_FOUND);
        }

        Page<ForumReply> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        Page<ForumReply> replyPage = baseMapper.selectChildReplyPage(page, queryDTO.getParentReplyId());

        // 转换为VO分页
        Page<ReplyVO> voPage = new Page<>(replyPage.getCurrent(), replyPage.getSize(), replyPage.getTotal());
        voPage.setRecords(toVOList(replyPage.getRecords()));
        return voPage;
    }

    /**
     * 批量转换为VO，一次性查询所有关联用户避免N+1问题
     */
    private List<ReplyVO> toVOList(List<ForumReply> entities) {
        if (entities.isEmpty()) {
            return List.of();
        }

        // 合并所有用户ID：创建者 + 被回复者
        Set<Long> userIds = new java.util.HashSet<>();
        entities.forEach(reply -> {
            userIds.add(reply.getCreatorId());
            userIds.add(reply.getReplyToUserId());
        });

        // 批量查询用户
        Map<Long, User> userMap = userIds.isEmpty()
                ? Map.of()
                : userService.listByIds(userIds).stream()
                        .collect(Collectors.toMap(User::getId, u -> u));

        return entities.stream()
                .map(entity -> toVO(entity, userMap))
                .toList();
    }

    private ReplyVO toVO(ForumReply entity, Map<Long, User> userMap) {
        ReplyVO vo = new ReplyVO();
        BeanUtils.copyProperties(entity, vo);

        // 已删除回复显示占位内容
        if (entity.getIsDeleted() == 1) {
            vo.setContent(ForumConstants.DELETED_REPLY_CONTENT);
        }

        // 设置创建者昵称
        User creator = userMap.get(entity.getCreatorId());
        if (creator != null) {
            vo.setCreatorNickname(creator.getNickname());
        }

        // 设置被回复者昵称
        User replyToUser = userMap.get(entity.getReplyToUserId());
        if (replyToUser != null) {
            vo.setReplyToUserNickname(replyToUser.getNickname());
        }

        return vo;
    }
}