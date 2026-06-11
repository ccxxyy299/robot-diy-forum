package com.slz.demo.server.controller;

import com.slz.demo.common.enumeration.ErrorCode;
import com.slz.demo.common.enumeration.UserRole;
import com.slz.demo.common.exception.BusinessException;
import com.slz.demo.common.result.Result;
import com.slz.demo.common.util.UserContext;
import com.slz.demo.pojo.entity.ForumAttachment;
import com.slz.demo.pojo.entity.ForumReply;
import com.slz.demo.pojo.entity.ForumTopic;
import com.slz.demo.server.constant.AttachmentConstants;
import com.slz.demo.server.service.ForumAttachmentService;
import com.slz.demo.server.service.ForumReplyService;
import com.slz.demo.server.service.ForumTopicService;
import com.slz.demo.server.service.MinioService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 附件
 */
@Slf4j
@RestController
@RequestMapping("/attachment")
@RequiredArgsConstructor
public class ForumAttachmentController {

    private final ForumAttachmentService attachmentService;
    private final ForumTopicService topicService;
    private final ForumReplyService replyService;
    private final MinioService minioService;

    /**
     * 获取附件预览 URL
     * @param id 附件ID
     * @return 签名 URL
     */
    @GetMapping("/url/{id}")
    public Result<String> getUrl(@PathVariable Long id) {
        ForumAttachment attachment = getVisibleAttachment(id);
        String presignedUrl = minioService.getPresignedUrl(attachment.getFilePath());
        return Result.success(presignedUrl);
    }

    /**
     * 获取附件下载 URL
     * @param id 附件ID
     * @return 签名 URL
     */
    @GetMapping("/download-url/{id}")
    public Result<String> getDownloadUrl(@PathVariable Long id) {
        ForumAttachment attachment = getVisibleAttachment(id);
        String downloadUrl = minioService.getDownloadPresignedUrl(attachment.getFilePath(), attachment.getFileName());
        return Result.success(downloadUrl);
    }

    /**
     * 获取附件并校验可见性
     * 管理员可访问所有附件（包括隐藏帖子的附件）
     */
    private ForumAttachment getVisibleAttachment(Long id) {
        ForumAttachment attachment = attachmentService.getById(id);
        if (attachment == null) {
            throw new BusinessException(ErrorCode.ATTACHMENT_NOT_FOUND);
        }

        // 管理员跳过可见性校验
        if (UserContext.get() != null && UserContext.get().getRole() == UserRole.ADMIN) {
            return attachment;
        }

        // 普通用户/游客：校验归属内容是否可见
        if (AttachmentConstants.RELATED_TYPE_TOPIC.equals(attachment.getRelatedType())) {
            ForumTopic topic = topicService.getById(attachment.getRelatedId());
            if (topic == null || topic.getStatus() == 0) {
                throw new BusinessException(ErrorCode.ATTACHMENT_NOT_FOUND);
            }
        } else if (AttachmentConstants.RELATED_TYPE_REPLY.equals(attachment.getRelatedType())) {
            ForumReply reply = replyService.getById(attachment.getRelatedId());
            if (reply == null || reply.getIsDeleted() == 1) {
                throw new BusinessException(ErrorCode.ATTACHMENT_NOT_FOUND);
            }
            ForumTopic topic = topicService.getById(reply.getTopicId());
            if (topic == null || topic.getStatus() == 0) {
                throw new BusinessException(ErrorCode.ATTACHMENT_NOT_FOUND);
            }
        }

        return attachment;
    }
}
