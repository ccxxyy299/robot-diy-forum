package com.slz.demo.server.service;

import org.springframework.web.multipart.MultipartFile;

/**
 * MinIO 对象存储操作服务
 */
public interface MinioService {

    /**
     * 上传文件到 MinIO
     * @param file 文件
     * @param objectName 对象名（如 avatar/1_1718000000.jpg）
     * @return 对象名
     */
    String upload(MultipartFile file, String objectName);

    /**
     * 生成签名访问 URL（预览/在线查看）
     * @param objectName 对象名
     * @return 签名 URL（带有效期）
     */
    String getPresignedUrl(String objectName);

    /**
     * 生成签名下载 URL（浏览器下载）
     * @param objectName 对象名
     * @param fileName 下载时的文件名
     * @return 签名 URL（带下载头）
     */
    String getDownloadPresignedUrl(String objectName, String fileName);

    /**
     * 删除文件
     * @param objectName 对象名
     */
    void delete(String objectName);
}
