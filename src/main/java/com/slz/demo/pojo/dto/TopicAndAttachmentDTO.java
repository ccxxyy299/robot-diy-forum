package com.slz.demo.pojo.dto;

import lombok.Data;

import java.util.List;

/**
 * 新增主题帖 + 附件请求
 */
@Data
public class TopicAndAttachmentDTO {

    /**
     * 主题帖业务信息
     */
    private TopicDTO topic;

    /**
     * 附件列表
     */
    private List<AttachmentUploadDTO> attachments;
}