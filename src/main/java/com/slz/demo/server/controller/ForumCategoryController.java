package com.slz.demo.server.controller;

import com.slz.demo.common.result.Result;
import com.slz.demo.pojo.dto.CategoryDTO;
import com.slz.demo.pojo.vo.CategoryTreeVO;
import com.slz.demo.pojo.vo.CategoryVO;
import com.slz.demo.server.service.ForumCategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 分类
 */
@RestController
@RequestMapping("/category")
@RequiredArgsConstructor
public class ForumCategoryController {

    private final ForumCategoryService forumCategoryService;


    /**
     * 新增分类
     */
    @PostMapping
    public Result<String> add(@Valid @RequestBody CategoryDTO dto) {
        forumCategoryService.add(dto);
        return Result.success("新增成功");
    }

    /**
     * 编辑分类
     */
    @PutMapping("/{id}")
    public Result<String> edit(@PathVariable Long id, @Valid @RequestBody CategoryDTO dto) {
        forumCategoryService.edit(id, dto);
        return Result.success("编辑成功");
    }

    /**
     * 删除分类
     */
    @DeleteMapping("/{id}")
    public Result<String> remove(@PathVariable Long id) {
        forumCategoryService.remove(id);
        return Result.success("删除成功");
    }

    /**
     * 查询所有父分类
     */
    @GetMapping("/parents")
    public Result<List<CategoryVO>> listParents() {
        return Result.success(forumCategoryService.listParents());
    }

    /**
     * 通过父分类ID查询子分类
     */
    @GetMapping("/children/{parentId}")
    public Result<List<CategoryVO>> listChildren(@PathVariable Long parentId) {
        return Result.success(forumCategoryService.listChildren(parentId));
    }

    /**
     * 根据分类名称模糊搜索
     */
    @GetMapping("/search")
    public Result<List<CategoryVO>> searchByName(@RequestParam String name) {
        return Result.success(forumCategoryService.searchByName(name));
    }

    /**
     * 查询所有分类
     */
    @GetMapping("/selectAll")
    public Result<List<CategoryTreeVO>> selectAll() {
        return Result.success(forumCategoryService.selectAll());
    }
}
