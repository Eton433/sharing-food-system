package com.example.sharingfood.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.core.sync.RequestBody;

import java.io.IOException;

@Service
public class S3Service {

    private final S3Client s3Client;
    private final String bucketName = "foodsharing-image"; // 替換為你的 S3 Bucket 名稱

    public S3Service(S3Client s3Client) {
        this.s3Client = s3Client;
    }

    public String uploadFile(MultipartFile file) {
        String key = "uploads/" + file.getOriginalFilename();

        try {
            s3Client.putObject(
                    PutObjectRequest.builder()
                            .bucket(bucketName)
                            .key(key)
                            .contentType(file.getContentType())
                            .acl("public-read") // 設置文件為公開讀取
                            .build(),
                    RequestBody.fromInputStream(file.getInputStream(), file.getSize())
            );
            return String.format("https://%s.s3.%s.amazonaws.com/%s", bucketName, "ap-southeast-1", key);
        } catch (IOException | S3Exception e) {
            throw new RuntimeException("Failed to upload file to S3: " + e.getMessage(), e);
        }
    }

}
