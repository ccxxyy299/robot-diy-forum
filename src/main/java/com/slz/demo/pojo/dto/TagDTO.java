package com.slz.demo.pojo.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 标签新增/修改请求
 */
@Data
public class TagDTO {

    @NotBlank(message = "标签名称不能为空")
    private String name;
}