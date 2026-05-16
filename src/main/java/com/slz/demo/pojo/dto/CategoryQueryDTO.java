package com.slz.demo.pojo.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 分类查询参数
 */
@Data
public class CategoryQueryDTO {

    /**
     * 是否分页
     */
    @NotNull(message = "isPage参数不能为空")
    private Boolean isPage;

    /**
     * 页码（分页时生效）
     */
    private Integer pageNum = 1;

    /**
     * 每页条数（分页时生效）
     */
    private Integer pageSize = 10;
}
