package com.slz.demo.pojo.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户分页查询参数
 */
@Data
public class UserPageQueryDTO {

    /**
     * 用户名
     */
    private String username;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 状态：1启用 0禁用
     */
    private Integer status;

    /**
     * 创建时间起始
     */
    private LocalDateTime createTimeStart;

    /**
     * 创建时间结束
     */
    private LocalDateTime createTimeEnd;

    /**
     * 页码
     */
    private Integer pageNum = 1;

    /**
     * 每页条数
     */
    private Integer pageSize = 10;
}