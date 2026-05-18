package com.slz.demo.pojo.dto;

import lombok.Data;

/**
 * 顶层回复分页查询参数
 */
@Data
public class ReplyTopQueryDTO {

    /**
     * 主题帖ID
     */
    private Long topicId;

    /**
     * 页码
     */
    private Integer pageNum = 1;

    /**
     * 每页条数
     */
    private Integer pageSize = 10;
}