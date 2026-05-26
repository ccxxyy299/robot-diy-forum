package com.slz.demo.common.util;

import com.slz.demo.common.enumeration.ErrorCode;
import com.slz.demo.common.exception.BusinessException;
import com.slz.demo.server.constant.AttachmentConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Set;
import java.util.UUID;

/**
 * 文件工具类
 */
@Slf4j
public class FileUtil {

    private static final Set<String> IMAGE_EXTENSIONS = Set.of("jpg", "jpeg", "png", "gif", "webp");
    private static final Set<String> FILE_EXTENSIONS = Set.of(
            "pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx", "txt", "zip", "rar"
    );

    public static String saveImage(MultipartFile file, String basePath, long maxImageSize) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ErrorCode.FILE_EMPTY);
        }
        if (file.getSize() > maxImageSize) {
            throw new BusinessException(ErrorCode.FILE_TOO_LARGE);
        }
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new BusinessException(ErrorCode.FILE_TYPE_INVALID);
        }

        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String filename = UUID.randomUUID().toString() + extension;

        File dir = new File(basePath, "avatar");
        if (!dir.exists()) {
            dir.mkdirs();
        }

        try {
            File dest = new File(dir, filename);
            file.transferTo(dest);
            log.info("头像上传成功: {} -> {}", originalFilename, dest.getAbsolutePath());
            return "avatar/" + filename;
        } catch (IOException e) {
            log.error("头像上传失败: {}", e.getMessage(), e);
            throw new BusinessException(ErrorCode.FILE_UPLOAD_FAILED);
        }
    }

    /**
     * 保存附件文件到磁盘
     * @param file 文件数据
     * @param basePath 基础路径
     * @param relatedType 关联类型：TOPIC / REPLY
     * @param relatedId 关联业务ID
     * @param fileType 文件类型：IMAGE / FILE
     * @param maxSize 最大文件大小（字节）
     * @return 相对路径
     */
    public static String saveAttachment(MultipartFile file, String basePath,
                                        String relatedType, Long relatedId, String fileType, long maxSize) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ErrorCode.FILE_EMPTY);
        }
        if (file.getSize() > maxSize) {
            throw new BusinessException(ErrorCode.FILE_TOO_LARGE);
        }

        // 校验文件扩展名
        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf(".") + 1).toLowerCase();
        }
        if (extension.isEmpty()) {
            throw new BusinessException(ErrorCode.FILE_TYPE_INVALID);
        }

        if (AttachmentConstants.FILE_TYPE_IMAGE.equals(fileType)) {
            if (!IMAGE_EXTENSIONS.contains(extension)) {
                throw new BusinessException(ErrorCode.FILE_TYPE_INVALID);
            }
        } else if (AttachmentConstants.FILE_TYPE_FILE.equals(fileType)) {
            if (!FILE_EXTENSIONS.contains(extension)) {
                throw new BusinessException(ErrorCode.FILE_TYPE_INVALID);
            }
        }

        // 按规则落盘：relatedType/fileType/relatedId/uuid.ext
        String subDir = relatedType.toLowerCase() + "/" + fileType.toLowerCase() + "/" + relatedId;
        String filename = UUID.randomUUID().toString() + "." + extension;

        File dir = new File(basePath, subDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        try {
            File dest = new File(dir, filename);
            file.transferTo(dest);
            log.info("附件上传成功: {} -> {}", originalFilename, dest.getAbsolutePath());
            return subDir + "/" + filename;
        } catch (IOException e) {
            log.error("附件上传失败: {}", e.getMessage(), e);
            throw new BusinessException(ErrorCode.FILE_UPLOAD_FAILED);
        }
    }

    /**
     * 获取文件扩展名（不含点）
     */
    public static String getExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
    }
}
