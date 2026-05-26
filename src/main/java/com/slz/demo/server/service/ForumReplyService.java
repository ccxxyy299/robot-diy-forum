package com.slz.demo.server.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.slz.demo.pojo.dto.ReplyAndAttachmentDTO;
import com.slz.demo.pojo.dto.ReplyDTO;
import com.slz.demo.pojo.dto.ReplyTopQueryDTO;
import com.slz.demo.pojo.dto.ReplyChildQueryDTO;
import com.slz.demo.pojo.entity.ForumReply;
import com.slz.demo.pojo.vo.ReplyVO;

/**
 * 回复 Service
 */
public interface ForumReplyService extends IService<ForumReply> {

    /**
     * 新增回复（支持附件）
     * @param dto 回复信息 + 附件
     * @return 新增的回复ID
     */
    Long add(ReplyAndAttachmentDTO dto);

    /**
     * 删除回复
     */
    void delete(Long id);

    /**
     * 分页查询顶层回复
     * @param queryDTO 查询参数
     * @return 分页结果
     */
    Page<ReplyVO> pageTopReply(ReplyTopQueryDTO queryDTO);

    /**
     * 分页查询子回复
     * @param queryDTO 查询参数
     * @return 分页结果
     */
    Page<ReplyVO> pageChildReply(ReplyChildQueryDTO queryDTO);

    /**
     * 查询当前用户的回复
     * @param pageNum 页码
     * @param pageSize 每页条数
     * @return 分页结果
     */
    Page<ReplyVO> myReplies(Integer pageNum, Integer pageSize);
}