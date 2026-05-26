package com.slz.demo.server.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.slz.demo.pojo.dto.AttachmentUploadDTO;
import com.slz.demo.pojo.entity.ForumAttachment;
import com.slz.demo.pojo.vo.AttachmentVO;

import java.util.List;
import java.util.Map;

/**
 * 附件 Service
 */
public interface ForumAttachmentService extends IService<ForumAttachment> {

    /**
     * 根据关联类型和ID查询附件列表
     * @param relatedType 关联类型：TOPIC / REPLY
     * @param relatedId 关联业务ID
     * @return 附件VO列表
     */
    List<AttachmentVO> listByRelated(String relatedType, Long relatedId);

    /**
     * 批量保存附件（含数量校验、文件落盘、失败回滚）
     * @param attachments 附件上传DTO列表
     * @param relatedType 关联类型：TOPIC / REPLY
     * @param relatedId 关联业务ID
     */
    void saveAttachments(List<AttachmentUploadDTO> attachments, String relatedType, Long relatedId);

    /**
     * 根据附件ID列表删除附件（含磁盘文件清理）
     * @param ids 附件ID列表
     * @param relatedId 关联业务ID（校验归属）
     */
    void deleteByIds(List<Long> ids, Long relatedId);

    /**
     * 根据关联类型和ID删除全部附件（含磁盘文件清理）
     * @param relatedType 关联类型
     * @param relatedId 关联业务ID
     */
    void deleteByRelated(String relatedType, Long relatedId);

    /**
     * 批量查询附件并按关联ID分组返回VO
     * @param relatedType 关联类型
     * @param relatedIds 关联业务ID列表
     * @return key=relatedId, value=附件VO列表
     */
    Map<Long, List<AttachmentVO>> mapByRelatedIds(String relatedType, List<Long> relatedIds);
}