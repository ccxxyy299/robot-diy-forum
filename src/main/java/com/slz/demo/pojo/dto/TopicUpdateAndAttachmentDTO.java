package com.slz.demo.pojo.dto;

import lombok.Data;

import java.util.List;

/**
 * 修改主题帖 + 附件请求
 */
@Data
public class TopicUpdateAndAttachmentDTO {

    /**
     * 主题帖业务信息
     */
    private TopicDTO topic;

    /**
     * 新增附件列表
     */
    private List<AttachmentUploadDTO> attachments;

    /**
     * 需要删除的附件ID列表
     */
    private List<Long> deleteAttachmentIds;
}