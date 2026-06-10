package com.slz.demo.pojo.dto;

import lombok.Data;

/**
 * 管理员主题帖分页查询参数
 */
@Data
public class AdminTopicQueryDTO {

    /**
     * 父分类ID（可选）
     */
    private Long parentId;

    /**
     * 子分类ID（可选）
     */
    private Long categoryId;

    /**
     * 标签ID（可选）
     */
    private Long tagId;

    /**
     * 帖子状态（可选）
     * 1=显示, 0=隐藏, 不传=查询全部
     */
    private Integer status;

    /**
     * 页码
     */
    private Integer pageNum = 1;

    /**
     * 每页条数
     */
    private Integer pageSize = 10;
}
