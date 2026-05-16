package com.slz.demo.common.util;

import com.slz.demo.common.enumeration.ErrorCode;
import com.slz.demo.common.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

/**
 * 文件工具类
 */
@Slf4j
public class FileUtil {

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
}
