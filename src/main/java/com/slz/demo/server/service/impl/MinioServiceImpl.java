package com.slz.demo.server.service.impl;

import com.slz.demo.server.config.MinioConfig;
import com.slz.demo.server.service.MinioService;
import io.minio.*;
import io.minio.http.Method;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * MinIO 对象存储操作实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MinioServiceImpl implements MinioService {

    private final MinioClient minioClient;
    private final MinioConfig minioConfig;

    @Override
    public String upload(MultipartFile file, String objectName) {
        try {
            // 确保存储桶存在
            ensureBucket();

            // 上传文件
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(minioConfig.getBucket())
                    .object(objectName)
                    .stream(file.getInputStream(), file.getSize(), -1)
                    .contentType(file.getContentType())
                    .build());

            log.info("文件上传成功: {}/{}", minioConfig.getBucket(), objectName);
            return objectName;
        } catch (Exception e) {
            log.error("文件上传失败: {}", objectName, e);
            throw new RuntimeException("文件上传失败", e);
        }
    }

    @Override
    public String getPresignedUrl(String objectName) {
        try {
            String url = minioClient.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder()
                    .method(Method.GET)
                    .bucket(minioConfig.getBucket())
                    .object(objectName)
                    .expiry(minioConfig.getUrlExpire(), TimeUnit.SECONDS)
                    .build());
            return url;
        } catch (Exception e) {
            log.error("生成签名URL失败: {}", objectName, e);
            throw new RuntimeException("生成签名URL失败", e);
        }
    }

    @Override
    public String getDownloadPresignedUrl(String objectName, String fileName) {
        try {
            String disposition = "attachment; filename=\"" + fileName + "\"";
            String url = minioClient.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder()
                    .method(Method.GET)
                    .bucket(minioConfig.getBucket())
                    .object(objectName)
                    .expiry(minioConfig.getUrlExpire(), TimeUnit.SECONDS)
                    .extraQueryParams(Map.of("response-content-disposition", disposition))
                    .build());
            return url;
        } catch (Exception e) {
            log.error("生成下载签名URL失败: {}", objectName, e);
            throw new RuntimeException("生成下载签名URL失败", e);
        }
    }

    @Override
    public void delete(String objectName) {
        try {
            minioClient.removeObject(RemoveObjectArgs.builder()
                    .bucket(minioConfig.getBucket())
                    .object(objectName)
                    .build());
            log.info("文件删除成功: {}/{}", minioConfig.getBucket(), objectName);
        } catch (Exception e) {
            log.error("文件删除失败: {}", objectName, e);
            throw new RuntimeException("文件删除失败", e);
        }
    }

    /**
     * 确保存储桶存在，不存在则创建
     */
    private void ensureBucket() throws Exception {
        boolean exists = minioClient.bucketExists(BucketExistsArgs.builder()
                .bucket(minioConfig.getBucket())
                .build());
        if (!exists) {
            minioClient.makeBucket(MakeBucketArgs.builder()
                    .bucket(minioConfig.getBucket())
                    .build());
            log.info("存储桶创建成功: {}", minioConfig.getBucket());
        }
    }
}
