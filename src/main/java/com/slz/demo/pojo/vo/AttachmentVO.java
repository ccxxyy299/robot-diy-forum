package com.slz.demo.pojo.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 附件返回
 */
@Data
public class AttachmentVO {

    /**
     * 附件ID
     */
    private Long id;

    /**
     * 文件名
     */
    private String fileName;

    /**
     * 文件大小（字节）
     */
    private Long fileSize;

    /**
     * 文件类型：IMAGE / FILE
     */
    private String fileType;

    /**
     * 图片附件预览地址（签名 URL）
     */
    private String url;

    /**
     * 关联类型：TOPIC / REPLY
     */
    private String relatedType;

    /**
     * 关联业务ID
     */
    private Long relatedId;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}