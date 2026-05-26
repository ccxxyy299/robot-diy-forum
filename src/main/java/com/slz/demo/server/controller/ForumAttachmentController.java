package com.slz.demo.server.controller;

import com.slz.demo.common.enumeration.ErrorCode;
import com.slz.demo.common.exception.BusinessException;
import com.slz.demo.pojo.entity.ForumAttachment;
import com.slz.demo.pojo.entity.ForumReply;
import com.slz.demo.pojo.entity.ForumTopic;
import com.slz.demo.server.constant.AttachmentConstants;
import com.slz.demo.server.service.ForumAttachmentService;
import com.slz.demo.server.service.ForumReplyService;
import com.slz.demo.server.service.ForumTopicService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaTypeFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

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

    @Value("${upload.path}")
    private String uploadPath;

    /**
     * 在线查看附件（图片预览）
     * @param id 附件ID
     * @param response HTTP响应
     */
    @GetMapping("/view/{id}")
    public void view(@PathVariable Long id, HttpServletResponse response) {
        ForumAttachment attachment = getVisibleAttachment(id);
        if (!AttachmentConstants.FILE_TYPE_IMAGE.equals(attachment.getFileType())) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "仅支持预览图片类型附件");
        }
        writeFile(attachment, response, "inline");
    }

    /**
     * 下载附件
     * @param id 附件ID
     * @param response HTTP响应
     */
    @GetMapping("/download/{id}")
    public void download(@PathVariable Long id, HttpServletResponse response) {
        ForumAttachment attachment = getVisibleAttachment(id);
        writeFile(attachment, response, "attachment");
    }

    /**
     * 输出文件流
     */
    private void writeFile(ForumAttachment attachment, HttpServletResponse response, String disposition) {
        File file = new File(uploadPath, attachment.getFilePath());

        if (!file.exists()) {
            try {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "文件不存在");
            } catch (IOException e) {
                log.error("发送404响应失败", e);
            }
            return;
        }

        // 根据文件名推断 MIME 类型，优先用 fileName，匹配不到用 filePath
        String contentType = MediaTypeFactory.getMediaType(attachment.getFileName())
                .or(() -> MediaTypeFactory.getMediaType(attachment.getFilePath()))
                .map(mt -> mt.toString())
                .orElse("application/octet-stream");

        try (FileInputStream fis = new FileInputStream(file);
             OutputStream os = response.getOutputStream()) {

            response.setContentType(contentType);
            String encodedFileName = URLEncoder.encode(attachment.getFileName(), StandardCharsets.UTF_8)
                    .replace("+", "%20");
            response.setHeader("Content-Disposition",
                    disposition + "; filename=\"" + encodedFileName + "\"; filename*=UTF-8''" + encodedFileName);
            response.setContentLengthLong(file.length());

            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                os.write(buffer, 0, bytesRead);
            }
            os.flush();

        } catch (IOException e) {
            log.error("文件输出失败：{}", e.getMessage(), e);
            try {
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "文件输出失败");
            } catch (IOException ex) {
                log.error("发送错误响应失败", ex);
            }
        }
    }

    /**
     * 获取附件并校验可见性
     */
    private ForumAttachment getVisibleAttachment(Long id) {
        ForumAttachment attachment = attachmentService.getById(id);
        if (attachment == null) {
            throw new BusinessException(ErrorCode.ATTACHMENT_NOT_FOUND);
        }

        // 校验归属内容是否可见
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
            // 回复所属的主题帖也必须可见
            ForumTopic topic = topicService.getById(reply.getTopicId());
            if (topic == null || topic.getStatus() == 0) {
                throw new BusinessException(ErrorCode.ATTACHMENT_NOT_FOUND);
            }
        }

        return attachment;
    }
}
