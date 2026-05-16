package com.slz.demo.server.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.slz.demo.common.enumeration.ErrorCode;
import com.slz.demo.common.exception.BusinessException;
import com.slz.demo.common.util.UserContext;
import com.slz.demo.pojo.dto.CategoryDTO;
import com.slz.demo.pojo.entity.ForumCategory;
import com.slz.demo.pojo.vo.CategoryTreeVO;
import com.slz.demo.pojo.vo.CategoryVO;
import com.slz.demo.server.mapper.ForumCategoryMapper;
import com.slz.demo.server.service.ForumCategoryService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 分类 Service 实现
 */
@Service
public class ForumCategoryServiceImpl extends ServiceImpl<ForumCategoryMapper, ForumCategory> implements ForumCategoryService {

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
        removeById(id);
    }

    @Override
    public List<CategoryTreeVO> selectAll() {
        List<ForumCategory> all = lambdaQuery()
                .orderByAsc(ForumCategory::getParentId)
                .orderByAsc(ForumCategory::getId)
                .list();

        List<CategoryTreeVO> voList = all.stream().map(this::toTreeVO).toList();
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

    private CategoryTreeVO toTreeVO(ForumCategory entity) {
        CategoryTreeVO vo = new CategoryTreeVO();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }

    @Override
    public List<CategoryVO> listParents() {
        List<ForumCategory> list = lambdaQuery()
                .eq(ForumCategory::getParentId, 0L)
                .orderByDesc(ForumCategory::getCreateTime)
                .list();
        return list.stream().map(this::toVO).toList();
    }

    @Override
    public List<CategoryVO> listChildren(Long parentId) {
        List<ForumCategory> list = lambdaQuery()
                .eq(ForumCategory::getParentId, parentId)
                .orderByDesc(ForumCategory::getCreateTime)
                .list();
        return list.stream().map(this::toVO).toList();
    }

    @Override
    public List<CategoryVO> searchByName(String name) {
        List<ForumCategory> list = lambdaQuery()
                .like(ForumCategory::getName, name)
                .orderByDesc(ForumCategory::getCreateTime)
                .list();
        return list.stream().map(this::toVO).toList();
    }

    private CategoryVO toVO(ForumCategory entity) {
        CategoryVO vo = new CategoryVO();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }
}
