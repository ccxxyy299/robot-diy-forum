package com.slz.demo.server.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.slz.demo.common.enumeration.ErrorCode;
import com.slz.demo.common.exception.BusinessException;
import com.slz.demo.common.util.UserContext;
import com.slz.demo.pojo.dto.TagDTO;
import com.slz.demo.pojo.entity.ForumTag;
import com.slz.demo.pojo.entity.User;
import com.slz.demo.pojo.vo.TagVO;
import com.slz.demo.server.mapper.ForumTagMapper;
import com.slz.demo.server.mapper.ForumTopicTagMapper;
import com.slz.demo.server.service.ForumTagService;
import com.slz.demo.server.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 标签 Service 实现
 */
@Service
@RequiredArgsConstructor
public class ForumTagServiceImpl extends ServiceImpl<ForumTagMapper, ForumTag> implements ForumTagService {

    private final UserService userService;
    private final ForumTopicTagMapper topicTagMapper;

    @Override
    public void add(TagDTO dto) {
        // 检查未删除的同名标签是否存在
        if (baseMapper.countByName(dto.getName()) > 0) {
            throw new BusinessException(ErrorCode.TAG_NAME_EXISTS);
        }

        ForumTag tag = new ForumTag();
        tag.setName(dto.getName());
        tag.setCreatorId(UserContext.get().getUserId());
        save(tag);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        ForumTag tag = getById(id);
        if (tag == null) {
            throw new BusinessException(ErrorCode.TAG_NOT_FOUND);
        }
        // 级联清理关联记录
        topicTagMapper.deleteByTagId(id);
        removeById(id);
    }

    @Override
    public void update(Long id, TagDTO dto) {
        ForumTag tag = getById(id);
        if (tag == null) {
            throw new BusinessException(ErrorCode.TAG_NOT_FOUND);
        }

        // 如果名称有变化，检查未删除的同名标签是否存在
        if (!dto.getName().equals(tag.getName())
                && baseMapper.countByNameExcludeId(dto.getName(), id) > 0) {
            throw new BusinessException(ErrorCode.TAG_NAME_EXISTS);
        }

        tag.setName(dto.getName());
        tag.setUpdateTime(LocalDateTime.now());
        updateById(tag);
    }

    @Override
    public List<TagVO> selectAll() {
        List<ForumTag> list = lambdaQuery()
                .orderByDesc(ForumTag::getCreateTime)
                .list();
        return toVOList(list);
    }

    @Override
    public List<TagVO> searchByName(String name) {
        List<ForumTag> list = lambdaQuery()
                .like(ForumTag::getName, name)
                .orderByDesc(ForumTag::getCreateTime)
                .list();
        return toVOList(list);
    }

    /**
     * 批量转换为VO，一次性查询用户信息避免N+1问题
     */
    private List<TagVO> toVOList(List<ForumTag> entities) {
        if (entities.isEmpty()) {
            return List.of();
        }

        // 批量查询用户
        Set<Long> creatorIds = entities.stream()
                .map(ForumTag::getCreatorId)
                .collect(Collectors.toSet());
        Map<Long, User> userMap = userService.listByIds(creatorIds)
                .stream()
                .collect(Collectors.toMap(User::getId, u -> u));

        return entities.stream().map(entity -> toVO(entity, userMap)).toList();
    }

    private TagVO toVO(ForumTag entity, Map<Long, User> userMap) {
        TagVO vo = new TagVO();
        BeanUtils.copyProperties(entity, vo);

        User user = userMap.get(entity.getCreatorId());
        if (user != null) {
            vo.setCreatorNickname(user.getNickname());
        }

        return vo;
    }
}