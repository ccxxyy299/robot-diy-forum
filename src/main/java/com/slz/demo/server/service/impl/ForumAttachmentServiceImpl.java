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
import com.slz.demo.server.service.MinioService;
import com.slz.demo.server.config.MinioConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.unit.DataSize;
import org.springframework.web.multipart.MultipartFile;

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

    private final MinioService minioService;
    private final MinioConfig minioConfig;

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
        if (imageCount > minioConfig.getMaxImageCount()) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "图片最多" + minioConfig.getMaxImageCount() + "张");
        }
        if (fileCount > minioConfig.getMaxFileCount()) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "附件最多" + minioConfig.getMaxFileCount() + "份");
        }

        Long userId = UserContext.get().getUserId();
        List<String> uploadedObjects = new ArrayList<>();

        try {
            for (AttachmentUploadDTO attachmentDTO : attachments) {
                if (attachmentDTO.getFileData() == null || attachmentDTO.getFileData().isEmpty()) {
                    continue;
                }

                MultipartFile file = attachmentDTO.getFileData();
                String fileType = attachmentDTO.getFileType();
                long maxSize = AttachmentConstants.FILE_TYPE_IMAGE.equals(fileType)
                        ? DataSize.parse(minioConfig.getMaxImageSize()).toBytes()
                        : DataSize.parse(minioConfig.getMaxFileSize()).toBytes();

                // 校验文件大小
                if (file.getSize() > maxSize) {
                    String typeDesc = AttachmentConstants.FILE_TYPE_IMAGE.equals(fileType) ? "图片" : "附件";
                    throw new BusinessException(ErrorCode.PARAM_ERROR, typeDesc + "大小超过限制");
                }

                // 确定文件名：前端传入 > 原始文件名 > UUID.ext
                String fileName = attachmentDTO.getFileName();
                if (fileName == null || fileName.isBlank()) {
                    fileName = file.getOriginalFilename();
                }
                if (fileName == null || fileName.isBlank()) {
                    String extension = FileUtil.getExtension(file.getOriginalFilename());
                    fileName = UUID.randomUUID().toString();
                    if (!extension.isEmpty()) {
                        fileName += "." + extension;
                    }
                }

                // 生成 MinIO 对象名：{relatedType小写}/{relatedId}_{timestamp}_{filename}
                String extension = FileUtil.getExtension(file.getOriginalFilename());
                String objectName = relatedType.toLowerCase() + "/"
                        + relatedId + "_" + System.currentTimeMillis() + "_" + UUID.randomUUID()
                        + (extension.isEmpty() ? "" : "." + extension);

                // 上传到 MinIO
                minioService.upload(file, objectName);
                uploadedObjects.add(objectName);

                // 保存数据库记录
                ForumAttachment attachment = new ForumAttachment();
                attachment.setFileName(fileName);
                attachment.setFilePath(objectName);
                attachment.setFileSize(file.getSize());
                attachment.setFileType(fileType);
                attachment.setRelatedType(relatedType);
                attachment.setRelatedId(relatedId);
                attachment.setUploaderId(userId);
                save(attachment);
            }
        } catch (Exception e) {
            // 回滚已上传到 MinIO 的文件
            for (String objectName : uploadedObjects) {
                try {
                    minioService.delete(objectName);
                } catch (Exception ex) {
                    log.warn("回滚 MinIO 文件删除失败: {}", objectName, ex);
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

        // 删除 MinIO 文件
        deleteMinioFiles(attachments);
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

        // 删除 MinIO 文件
        deleteMinioFiles(attachments);
    }

    /**
     * 删除 MinIO 上的附件文件
     */
    private void deleteMinioFiles(List<ForumAttachment> attachments) {
        for (ForumAttachment attachment : attachments) {
            try {
                minioService.delete(attachment.getFilePath());
            } catch (Exception e) {
                log.warn("MinIO 文件删除失败: {}", attachment.getFilePath(), e);
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

        // 生成签名 URL（有效期 1 小时）
        try {
            vo.setUrl(minioService.getPresignedUrl(entity.getFilePath()));
            vo.setDownloadUrl(minioService.getDownloadPresignedUrl(entity.getFilePath(), entity.getFileName()));
        } catch (Exception e) {
            log.warn("生成签名URL失败: {}", entity.getFilePath(), e);
        }

        vo.setRelatedType(entity.getRelatedType());
        vo.setRelatedId(entity.getRelatedId());
        vo.setCreateTime(entity.getCreateTime());
        return vo;
    }
}
