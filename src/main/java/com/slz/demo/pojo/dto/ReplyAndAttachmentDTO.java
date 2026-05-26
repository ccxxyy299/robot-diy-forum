package com.slz.demo.pojo.dto;

import lombok.Data;

import java.util.List;

/**
 * 新增回复 + 附件请求
 */
@Data
public class ReplyAndAttachmentDTO {

    /**
     * 回复业务信息
     */
    private ReplyDTO reply;

    /**
     * 附件列表
     */
    private List<AttachmentUploadDTO> attachments;
}