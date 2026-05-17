package com.slz.demo.pojo.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 主题帖返回
 */
@Data
public class TopicVO {

    private Long id;

    private Long categoryId;

    private String categoryName;

    private Long creatorId;

    private String creatorNickname;

    private String title;

    private String content;

    private Integer status;

    private Integer viewCount;

    private Integer replyCount;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    /**
     * 标签列表
     */
    private List<TagVO> tags;
}