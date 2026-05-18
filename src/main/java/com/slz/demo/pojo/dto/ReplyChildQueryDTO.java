package com.slz.demo.pojo.dto;

import lombok.Data;

/**
 * 子回复分页查询参数
 */
@Data
public class ReplyChildQueryDTO {

    /**
     * 父回复ID
     */
    private Long parentReplyId;

    /**
     * 页码
     */
    private Integer pageNum = 1;

    /**
     * 每页条数
     */
    private Integer pageSize = 10;
}