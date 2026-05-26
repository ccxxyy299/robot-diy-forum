package com.slz.demo.pojo.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

/**
 * 附件上传 DTO
 */
@Data
public class AttachmentUploadDTO {

    /**
     * 文件名称
     * 前端可传原始文件名，为空时以后端 MultipartFile 原始文件名为准
     */
    private String fileName;

    /**
     * 文件类型：IMAGE / FILE
     */
    private String fileType;

    /**
     * 文件数据
     */
    private MultipartFile fileData;
}