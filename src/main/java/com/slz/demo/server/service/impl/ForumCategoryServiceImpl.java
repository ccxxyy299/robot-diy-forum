package com.slz.demo.server.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.slz.demo.common.enumeration.ErrorCode;
import com.slz.demo.common.exception.BusinessException;
import com.slz.demo.common.util.UserContext;
import com.slz.demo.pojo.dto.CategoryDTO;
import com.slz.demo.pojo.entity.ForumCategory;
import com.slz.demo.pojo.entity.ForumTopic;
import com.slz.demo.pojo.entity.User;
import com.slz.demo.pojo.vo.CategoryTreeVO;
import com.slz.demo.pojo.vo.CategoryVO;
import com.slz.demo.server.mapper.ForumCategoryMapper;
import com.slz.demo.server.mapper.ForumTopicMapper;
import com.slz.demo.server.service.ForumCategoryService;
import com.slz.demo.server.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 分类 Service 实现
 */
@Service
@RequiredArgsConstructor
public class ForumCategoryServiceImpl extends ServiceImpl<ForumCategoryMapper, ForumCategory> implements ForumCategoryService {

    private final UserService userService;
    private final ForumTopicMapper forumTopicMapper;

    @Override
    public void add(CategoryDTO dto) {
        if (dto.getParentId() == null) {
            throw new BusinessException(ErrorCode.PARENT_ID_NULL);
        }
        Long parentId = dto.getParentId();
        if (parentId != 0 && !lambdaQuery().eq(ForumCategory::getId, parentId).exists()) {
            throw new BusinessException(ErrorCode.CATEGORY_NOT_FOUND);
        }
        if (lambdaQuery()
                .eq(ForumCategory::getParentId, parentId)
                .eq(ForumCategory::getName, dto.getName())
                .exists()) {
            throw new BusinessException(ErrorCode.CATEGORY_NAME_DUPLICATE);
        }

        ForumCategory category = new ForumCategory();
        category.setParentId(parentId);
        category.setName(dto.getName());
        category.setDescription(dto.getDescription());
        category.setCreatorId(UserContext.get().getUserId());
        save(category);
    }

    @Override
    public void edit(Long id, CategoryDTO dto) {
        ForumCategory category = getById(id);
        if (category == null) {
            throw new BusinessException(ErrorCode.CATEGORY_NOT_FOUND);
        }
        if (dto.getParentId() == null) {
            throw new BusinessException(ErrorCode.PARENT_ID_NULL);
        }
        if (id.equals(dto.getParentId())) {
            throw new BusinessException(ErrorCode.CATEGORY_PARENT_SELF);
        }

        Long parentId = dto.getParentId();
        if (parentId != 0 && !lambdaQuery().eq(ForumCategory::getId, parentId).exists()) {
            throw new BusinessException(ErrorCode.CATEGORY_NOT_FOUND);
        }
        if (!dto.getName().equals(category.getName())
                && lambdaQuery()
                .eq(ForumCategory::getParentId, parentId)
                .eq(ForumCategory::getName, dto.getName())
                .exists()) {
            throw new BusinessException(ErrorCode.CATEGORY_NAME_DUPLICATE);
        }

        category.setParentId(parentId);
        category.setName(dto.getName());
        category.setDescription(dto.getDescription());
        category.setCreatorId(UserContext.get().getUserId());
        category.setUpdateTime(LocalDateTime.now());
        updateById(category);
    }

    @Override
    public void remove(Long id) {
        ForumCategory category = getById(id);
        if (category == null) {
            throw new BusinessException(ErrorCode.CATEGORY_NOT_FOUND);
        }
        if (lambdaQuery().eq(ForumCategory::getParentId, id).exists()) {
            throw new BusinessException(ErrorCode.CATEGORY_HAS_CHILDREN);
        }
        LambdaQueryWrapper<ForumTopic> topicWrapper = new LambdaQueryWrapper<>();
        topicWrapper.eq(ForumTopic::getCategoryId, id);
        if (forumTopicMapper.exists(topicWrapper)) {
            throw new BusinessException(ErrorCode.CATEGORY_HAS_TOPICS);
        }
        removeById(id);
    }

    @Override
    public List<CategoryTreeVO> selectAll() {
        List<ForumCategory> all = lambdaQuery()
                .orderByAsc(ForumCategory::getParentId)
                .orderByAsc(ForumCategory::getId)
                .list();

        // 批量查询用户
        Set<Long> creatorIds = all.stream()
                .map(ForumCategory::getCreatorId)
                .collect(Collectors.toSet());
        Map<Long, User> userMap = userService.listByIds(creatorIds)
                .stream()
                .collect(Collectors.toMap(User::getId, u -> u));

        List<CategoryTreeVO> voList = all.stream().map(e -> toTreeVO(e, userMap)).toList();
        // 按 parentId 分组
        Map<Long, List<CategoryTreeVO>> childrenMap = voList.stream()
                .collect(Collectors.groupingBy(CategoryTreeVO::getParentId));

        // 构建树：parentId=0 的为根节点
        List<CategoryTreeVO> roots = childrenMap.getOrDefault(0L, new ArrayList<>());
        for (CategoryTreeVO root : roots) {
            buildTree(root, childrenMap);
        }
        return roots;
    }

    private void buildTree(CategoryTreeVO parent, Map<Long, List<CategoryTreeVO>> childrenMap) {
        List<CategoryTreeVO> children = childrenMap.getOrDefault(parent.getId(), new ArrayList<>());
        parent.setChildren(children);
        for (CategoryTreeVO child : children) {
            buildTree(child, childrenMap);
        }
    }

    private CategoryTreeVO toTreeVO(ForumCategory entity, Map<Long, User> userMap) {
        CategoryTreeVO vo = new CategoryTreeVO();
        BeanUtils.copyProperties(entity, vo);

        User user = userMap.get(entity.getCreatorId());
        if (user != null) {
            vo.setCreatorNickname(user.getNickname());
        }

        return vo;
    }

    @Override
    public List<CategoryVO> listParents() {
        List<ForumCategory> list = lambdaQuery()
                .eq(ForumCategory::getParentId, 0L)
                .orderByDesc(ForumCategory::getCreateTime)
                .list();
        return toVOList(list);
    }

    @Override
    public List<CategoryVO> listChildren(Long parentId) {
        List<ForumCategory> list = lambdaQuery()
                .eq(ForumCategory::getParentId, parentId)
                .orderByDesc(ForumCategory::getCreateTime)
                .list();
        return toVOList(list);
    }

    @Override
    public List<CategoryVO> searchByName(String name) {
        List<ForumCategory> list = lambdaQuery()
                .like(ForumCategory::getName, name)
                .orderByDesc(ForumCategory::getCreateTime)
                .list();
        return toVOList(list);
    }

    /**
     * 批量转换为VO，一次性查询用户信息避免N+1问题
     */
    private List<CategoryVO> toVOList(List<ForumCategory> entities) {
        if (entities.isEmpty()) {
            return List.of();
        }

        Set<Long> creatorIds = entities.stream()
                .map(ForumCategory::getCreatorId)
                .collect(Collectors.toSet());
        Map<Long, User> userMap = userService.listByIds(creatorIds)
                .stream()
                .collect(Collectors.toMap(User::getId, u -> u));

        return entities.stream().map(e -> toVO(e, userMap)).toList();
    }

    private CategoryVO toVO(ForumCategory entity, Map<Long, User> userMap) {
        CategoryVO vo = new CategoryVO();
        BeanUtils.copyProperties(entity, vo);

        User user = userMap.get(entity.getCreatorId());
        if (user != null) {
            vo.setCreatorNickname(user.getNickname());
        }

        return vo;
    }
}
