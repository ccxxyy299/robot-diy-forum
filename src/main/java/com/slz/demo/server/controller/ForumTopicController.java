package com.slz.demo.server.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.slz.demo.common.result.Result;
import com.slz.demo.pojo.dto.TopicDTO;
import com.slz.demo.pojo.dto.TopicQueryDTO;
import com.slz.demo.pojo.vo.TopicVO;
import com.slz.demo.server.service.ForumTopicService;
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
 * 主题帖
 */
@RestController
@RequestMapping("/topic")
@RequiredArgsConstructor
public class ForumTopicController {

    private final ForumTopicService forumTopicService;

    /**
     * 新增主题帖
     * @param dto 主题帖信息
     * @return 结果
     */
    @PostMapping
    public Result<String> add(@Valid @RequestBody TopicDTO dto) {
        forumTopicService.add(dto);
        return Result.success("新增成功");
    }

    /**
     * 删除主题帖
     * @param id 主题帖ID
     * @return 结果
     */
    @DeleteMapping("/{id}")
    public Result<String> delete(@PathVariable Long id) {
        forumTopicService.delete(id);
        return Result.success("删除成功");
    }

    /**
     * 修改主题帖
     * @param id 主题帖ID
     * @param dto 主题帖信息
     * @return 结果
     */
    @PutMapping("/{id}")
    public Result<String> update(@PathVariable Long id, @Valid @RequestBody TopicDTO dto) {
        forumTopicService.update(id, dto);
        return Result.success("修改成功");
    }

    /**
     * 查询所有主题帖
     * @return 所有主题帖列表
     */
    @GetMapping("/selectAll")
    public Result<List<TopicVO>> selectAll() {
        return Result.success(forumTopicService.selectAll());
    }

    /**
     * 分页查询主题帖
     * 支持按父分类、子分类、标签组合查询
     * @param queryDTO 查询参数
     * @return 分页结果
     */
    @PostMapping("/page")
    public Result<Page<TopicVO>> page(@RequestBody TopicQueryDTO queryDTO) {
        return Result.success(forumTopicService.page(queryDTO));
    }

    /**
     * 查询主题帖详情
     * @param id 主题帖ID
     * @return 主题帖详情
     */
    @GetMapping("/detail/{id}")
    public Result<TopicVO> detail(@PathVariable Long id) {
        return Result.success(forumTopicService.detail(id));
    }

    /**
     * 修改主题帖状态（显示/隐藏）
     * @param id 主题帖ID
     * @param status 目标状态（true显示 false隐藏）
     * @return 结果
     */
    @PutMapping("/{id}/status")
    public Result<String> updateTopicStatus(@PathVariable Long id, @RequestParam boolean status) {
        forumTopicService.updateTopicStatus(id, status);
        return Result.success("修改成功");
    }

    /**
     * 查询当前用户的主题帖
     * @param pageNum 页码
     * @param pageSize 每页条数
     * @return 分页结果
     */
    @GetMapping("/my")
    public Result<Page<TopicVO>> myTopics(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(forumTopicService.myTopics(pageNum, pageSize));
    }
}