package com.slz.demo.server.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.slz.demo.common.result.Result;
import com.slz.demo.pojo.dto.ReplyDTO;
import com.slz.demo.pojo.dto.ReplyTopQueryDTO;
import com.slz.demo.pojo.dto.ReplyChildQueryDTO;
import com.slz.demo.pojo.vo.ReplyVO;
import com.slz.demo.server.service.ForumReplyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 回复
 */
@RestController
@RequestMapping("/reply")
@RequiredArgsConstructor
public class ForumReplyController {

    private final ForumReplyService forumReplyService;

    /**
     * 新增回复
     * @param dto 回复信息
     * @return 结果
     */
    @PostMapping
    public Result<String> add(@Valid @RequestBody ReplyDTO dto) {
        forumReplyService.add(dto);
        return Result.success("新增成功");
    }

    /**
     * 删除回复
     * @param id 回复ID
     * @return 结果
     */
    @DeleteMapping("/{id}")
    public Result<String> delete(@PathVariable Long id) {
        forumReplyService.delete(id);
        return Result.success("删除成功");
    }

    /**
     * 分页查询顶层回复
     * @param queryDTO 查询参数
     * @return 分页结果
     */
    @PostMapping("/top/page")
    public Result<Page<ReplyVO>> pageTopReply(@RequestBody ReplyTopQueryDTO queryDTO) {
        return Result.success(forumReplyService.pageTopReply(queryDTO));
    }

    /**
     * 分页查询子回复
     * @param queryDTO 查询参数
     * @return 分页结果
     */
    @PostMapping("/child/page")
    public Result<Page<ReplyVO>> c(@RequestBody ReplyChildQueryDTO queryDTO) {
        return Result.success(forumReplyService.pageChildReply(queryDTO));
    }
}