package com.slz.demo.pojo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * 主题帖新增/修改请求
 */
@Data
public class TopicDTO {

    @NotNull(message = "分类ID不能为空")
    private Long categoryId;

    @NotBlank(message = "标题不能为空")
    private String title;

    @NotBlank(message = "内容不能为空")
    private String content;

    /**
     * 标签ID列表，可选，可为空
     */
    private List<Long> tagIds;
}