package com.slz.demo.server.controller;

import com.slz.demo.common.enumeration.UserRole;
import com.slz.demo.common.result.Result;
import com.slz.demo.pojo.dto.TagDTO;
import com.slz.demo.pojo.vo.TagVO;
import com.slz.demo.server.annotation.RoleRequired;
import com.slz.demo.server.service.ForumTagService;
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
 * 标签接口
 */
@RestController
@RequestMapping("/tag")
@RequiredArgsConstructor
public class ForumTagController {

    private final ForumTagService forumTagService;

    /**
     * 新增标签
     * @param dto 标签信息
     * @return 结果
     */
    @PostMapping
    @RoleRequired(UserRole.ADMIN)
    public Result<String> add(@Valid @RequestBody TagDTO dto) {
        forumTagService.add(dto);
        return Result.success("新增成功");
    }

    /**
     * 删除标签
     * @param id 标签ID
     * @return 结果
     */
    @DeleteMapping("/{id}")
    @RoleRequired(UserRole.ADMIN)
    public Result<String> delete(@PathVariable Long id) {
        forumTagService.delete(id);
        return Result.success("删除成功");
    }

    /**
     * 修改标签
     * @param id 标签ID
     * @param dto 标签信息
     * @return 结果
     */
    @PutMapping("/{id}")
    @RoleRequired(UserRole.ADMIN)
    public Result<String> update(@PathVariable Long id, @Valid @RequestBody TagDTO dto) {
        forumTagService.update(id, dto);
        return Result.success("修改成功");
    }

    /**
     * 查询所有标签
     * @return 所有标签列表
     */
    @GetMapping("/selectAll")
    public Result<List<TagVO>> selectAll() {
        return Result.success(forumTagService.selectAll());
    }

    /**
     * 根据标签名称模糊搜索
     * @param name 标签名称
     * @return 匹配的标签列表
     */
    @GetMapping("/search")
    public Result<List<TagVO>> searchByName(@RequestParam String name) {
        return Result.success(forumTagService.searchByName(name));
    }
}
