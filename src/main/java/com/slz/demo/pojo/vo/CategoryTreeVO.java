package com.slz.demo.pojo.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 分类树返回
 */
@Data
public class CategoryTreeVO {

    private Long id;

    private Long parentId;

    private String name;

    private String description;

    private Long creatorId;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    private Integer isDeleted;

    private List<CategoryTreeVO> children;
}