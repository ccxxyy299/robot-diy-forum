package com.slz.demo.pojo.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 分类返回
 */
@Data
public class CategoryVO {

    private Long id;

    private Long parentId;

    private String name;

    private String description;

    private Long creatorId;

    private String creatorNickname;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    private Integer isDeleted;
}
