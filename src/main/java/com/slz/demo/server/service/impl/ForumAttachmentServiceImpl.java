package com.slz.demo.server.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.slz.demo.common.enumeration.ErrorCode;
import com.slz.demo.common.exception.BusinessException;
import com.slz.demo.common.util.FileUtil;
import com.slz.demo.common.util.UserContext;
import com.slz.demo.pojo.dto.AttachmentUploadDTO;
import com.slz.demo.pojo.entity.ForumAttachment;
import com.slz.demo.pojo.vo.AttachmentVO;
import com.slz.demo.server.constant.AttachmentConstants;
import com.slz.demo.server.mapper.ForumAttachmentMapper;
import com.slz.demo.server.service.ForumAttachmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.unit.DataSize;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 附件 Service 实现
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ForumAttachmentServiceImpl extends ServiceImpl<ForumAttachmentMapper, ForumAttachment> implements ForumAttachmentService {

    @Value("${upload.path}")
    private String uploadPath;

    @Value("${upload.base-url}")
    private String baseUrl;

    @Value("${upload.max-image-size}")
    private DataSize maxImageSize;

    @Value("${upload.max-file-size}")
    private DataSize maxFileSize;

    @Value("${upload.max-image-count}")
    private int maxImageCount;

    @Value("${upload.max-file-count}")
    private int maxFileCount;

    @Override
    public List<AttachmentVO> listByRelated(String relatedType, Long relatedId) {
        List<ForumAttachment> list = lambdaQuery()
                .eq(ForumAttachment::getRelatedType, relatedType)
                .eq(ForumAttachment::getRelatedId, relatedId)
                .orderByAsc(ForumAttachment::getCreateTime)
                .list();

        return list.stream().map(this::toVO).toList();
    }

    @Override
    public void saveAttachments(List<AttachmentUploadDTO> attachments, String relatedType, Long relatedId) {
        if (attachments == null || attachments.isEmpty()) {
            return;
        }

        // 校验 fileType 合法性
        for (AttachmentUploadDTO dto : attachments) {
            if (dto.getFileData() == null || dto.getFileData().isEmpty()) {
                continue;
            }
            String fileType = dto.getFileType();
            if (fileType == null || fileType.isBlank()) {
                throw new BusinessException(ErrorCode.PARAM_ERROR, "文件类型不能为空");
            }
            if (!AttachmentConstants.FILE_TYPE_IMAGE.equals(fileType)
                    && !AttachmentConstants.FILE_TYPE_FILE.equals(fileType)) {
                throw new BusinessException(ErrorCode.PARAM_ERROR, "文件类型只能为 IMAGE 或 FILE");
            }
        }

        // 校验附件数量
        long imageCount = attachments.stream()
                .filter(a -> AttachmentConstants.FILE_TYPE_IMAGE.equals(a.getFileType())).count();
        long fileCount = attachments.stream()
                .filter(a -> AttachmentConstants.FILE_TYPE_FILE.equals(a.getFileType())).count();
        if (imageCount > maxImageCount) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "图片最多" + maxImageCount + "张");
        }
        if (fileCount > maxFileCount) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "附件最多" + maxFileCount + "份");
        }

        Long userId = UserContext.get().getUserId();
        List<File> savedFiles = new ArrayList<>();

        try {
            for (AttachmentUploadDTO attachmentDTO : attachments) {
                if (attachmentDTO.getFileData() == null || attachmentDTO.getFileData().isEmpty()) {
                    continue;
                }

                String fileType = attachmentDTO.getFileType();
                long maxSize = AttachmentConstants.FILE_TYPE_IMAGE.equals(fileType) ? maxImageSize.toBytes() : maxFileSize.toBytes();
                String filePath = FileUtil.saveAttachment(
                        attachmentDTO.getFileData(), uploadPath, relatedType, relatedId, fileType, maxSize);

                // 记录已保存的文件，失败时回滚
                savedFiles.add(new File(uploadPath, filePath));

                // 确定文件名：前端传入 > 原始文件名 > UUID.ext
                String fileName = attachmentDTO.getFileName();
                if (fileName == null || fileName.isBlank()) {
                    fileName = attachmentDTO.getFileData().getOriginalFilename();
                }
                if (fileName == null || fileName.isBlank()) {
                    String extension = FileUtil.getExtension(
                            attachmentDTO.getFileData().getOriginalFilename());
                    fileName = UUID.randomUUID().toString();
                    if (!extension.isEmpty()) {
                        fileName += "." + extension;
                    }
                }

                ForumAttachment attachment = new ForumAttachment();
                attachment.setFileName(fileName);
                attachment.setFilePath(filePath);
                attachment.setFileSize(attachmentDTO.getFileData().getSize());
                attachment.setFileType(fileType);
                attachment.setRelatedType(relatedType);
                attachment.setRelatedId(relatedId);
                attachment.setUploaderId(userId);
                save(attachment);
            }
        } catch (Exception e) {
            // 回滚已保存的文件
            for (File file : savedFiles) {
                if (file.exists()) {
                    if (!file.delete()) {
                        log.warn("回滚文件删除失败: {}", file.getAbsolutePath());
                    }
                }
            }
            throw e;
        }
    }

    @Override
    public void deleteByIds(List<Long> ids, Long relatedId) {
        if (ids == null || ids.isEmpty()) {
            return;
        }
        // 校验附件是否属于该关联业务
        List<ForumAttachment> attachments = listByIds(ids);
        Map<Long, ForumAttachment> attachmentMap = attachments.stream()
                .collect(Collectors.toMap(ForumAttachment::getId, a -> a));

        for (Long id : ids) {
            ForumAttachment attachment = attachmentMap.get(id);
            if (attachment == null) {
                throw new BusinessException(ErrorCode.ATTACHMENT_NOT_FOUND);
            }
            if (!attachment.getRelatedId().equals(relatedId)) {
                throw new BusinessException(ErrorCode.NO_PERMISSION, "附件不属于该内容");
            }
        }

        // 删除数据库记录
        removeByIds(ids);

        // 删除磁盘文件
        deleteDiskFiles(attachments);
    }

    @Override
    public void deleteByRelated(String relatedType, Long relatedId) {
        List<ForumAttachment> attachments = lambdaQuery()
                .eq(ForumAttachment::getRelatedType, relatedType)
                .eq(ForumAttachment::getRelatedId, relatedId)
                .list();

        if (attachments.isEmpty()) {
            return;
        }

        // 删除数据库记录
        List<Long> ids = attachments.stream().map(ForumAttachment::getId).toList();
        removeByIds(ids);

        // 删除磁盘文件
        deleteDiskFiles(attachments);
    }

    /**
     * 删除磁盘上的附件文件
     */
    private void deleteDiskFiles(List<ForumAttachment> attachments) {
        for (ForumAttachment attachment : attachments) {
            File file = new File(uploadPath, attachment.getFilePath());
            if (file.exists()) {
                if (!file.delete()) {
                    log.warn("磁盘文件删除失败: {}", file.getAbsolutePath());
                }
            }
        }
    }

    @Override
    public Map<Long, List<AttachmentVO>> mapByRelatedIds(String relatedType, List<Long> relatedIds) {
        if (relatedIds == null || relatedIds.isEmpty()) {
            return Map.of();
        }

        List<ForumAttachment> allAttachments = lambdaQuery()
                .eq(ForumAttachment::getRelatedType, relatedType)
                .in(ForumAttachment::getRelatedId, relatedIds)
                .orderByAsc(ForumAttachment::getCreateTime)
                .list();

        return allAttachments.stream()
                .collect(Collectors.groupingBy(
                        ForumAttachment::getRelatedId,
                        Collectors.mapping(this::toVO, Collectors.toList())
                ));
    }

    private AttachmentVO toVO(ForumAttachment entity) {
        AttachmentVO vo = new AttachmentVO();
        vo.setId(entity.getId());
        vo.setFileName(entity.getFileName());
        vo.setFileSize(entity.getFileSize());
        vo.setFileType(entity.getFileType());
        // 图片：通过 view 接口访问（有可见性校验）；文件：null
        if (AttachmentConstants.FILE_TYPE_IMAGE.equals(entity.getFileType())) {
            vo.setUrl(baseUrl + "/attachment/view/" + entity.getId());
        }
        // 所有附件都可下载
        vo.setDownloadUrl(baseUrl + "/attachment/download/" + entity.getId());
        vo.setRelatedType(entity.getRelatedType());
        vo.setRelatedId(entity.getRelatedId());
        vo.setCreateTime(entity.getCreateTime());
        return vo;
    }
}