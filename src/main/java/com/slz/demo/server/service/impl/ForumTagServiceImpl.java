package com.slz.demo.server.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.slz.demo.common.enumeration.ErrorCode;
import com.slz.demo.common.exception.BusinessException;
import com.slz.demo.common.util.UserContext;
import com.slz.demo.pojo.dto.TagDTO;
import com.slz.demo.pojo.entity.ForumTag;
import com.slz.demo.pojo.vo.TagVO;
import com.slz.demo.server.mapper.ForumTagMapper;
import com.slz.demo.server.service.ForumTagService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 标签 Service 实现
 */
@Service
public class ForumTagServiceImpl extends ServiceImpl<ForumTagMapper, ForumTag> implements ForumTagService {

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
    public void delete(Long id) {
        ForumTag tag = getById(id);
        if (tag == null) {
            throw new BusinessException(ErrorCode.TAG_NOT_FOUND);
        }
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
        return list.stream().map(this::toVO).toList();
    }

    @Override
    public List<TagVO> searchByName(String name) {
        List<ForumTag> list = lambdaQuery()
                .like(ForumTag::getName, name)
                .orderByDesc(ForumTag::getCreateTime)
                .list();
        return list.stream().map(this::toVO).toList();
    }

    private TagVO toVO(ForumTag entity) {
        TagVO vo = new TagVO();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }
}