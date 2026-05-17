package com.slz.demo.pojo.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 标签返回
 */
@Data
public class TagVO {

    private Long id;

    private String name;

    private Long creatorId;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    private Integer isDeleted;
}