package com.slz.demo.pojo.dto;

import lombok.Data;

/**
 * 主题帖分页查询参数
 */
@Data
public class TopicQueryDTO {

    /**
     * 父分类ID（可选）
     * 传递时查询该父分类下所有子分类的主题帖
     */
    private Long parentId;

    /**
     * 子分类ID（可选）
     * 传递时查询该子分类下的主题帖
     * 如果同时传递parentId和categoryId，优先使用categoryId
     */
    private Long categoryId;

    /**
     * 标签ID（可选）
     * 传递时筛选带有该标签的主题帖
     */
    private Long tagId;

    /**
     * 页码
     */
    private Integer pageNum = 1;

    /**
     * 每页条数
     */
    private Integer pageSize = 10;
}