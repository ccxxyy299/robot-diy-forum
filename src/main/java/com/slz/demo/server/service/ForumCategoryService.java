package com.slz.demo.server.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.slz.demo.pojo.dto.CategoryDTO;
import com.slz.demo.pojo.entity.ForumCategory;
import com.slz.demo.pojo.vo.CategoryTreeVO;
import com.slz.demo.pojo.vo.CategoryVO;

import java.util.List;

/**
 * 分类 Service
 */
public interface ForumCategoryService extends IService<ForumCategory> {

    /**
     * 新增分类
     */
    void add(CategoryDTO dto);

    /**
     * 编辑分类
     */
    void edit(Long id, CategoryDTO dto);

    /**
     * 删除分类（有子分类时拒绝删除）
     */
    void remove(Long id);

    /**
     * 查询所有分类（树形结构）
     */
    List<CategoryTreeVO> selectAll();

    /**
     * 查询所有父分类（parent_id=0）
     */
    List<CategoryVO> listParents();

    /**
     * 通过父分类ID查询子分类
     */
    List<CategoryVO> listChildren(Long parentId);

    /**
     * 根据分类名称模糊搜索
     */
    List<CategoryVO> searchByName(String name);
}
