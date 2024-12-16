package com.example.sharingfood.service;

import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.core.sync.RequestBody;

import java.util.UUID;

@Service
public class S3Service {

    private final S3Client s3Client;
    private final String bucketName = "foodsharing-image"; // 替換為你的 S3 Bucket 名稱

    public S3Service(S3Client s3Client) {
        this.s3Client = s3Client;
    }

    public String uploadFile(byte[] fileBytes, String originalFilename, String contentType) {
        String key = "uploads/" + UUID.randomUUID() + "_" + originalFilename;

        try {
            s3Client.putObject(
                    PutObjectRequest.builder()
                            .bucket(bucketName)
                            .key(key)
                            .contentType(contentType)
                            .acl("public-read")
                            .build(),
                    RequestBody.fromBytes(fileBytes)
            );

            return String.format("https://%s.s3.%s.amazonaws.com/%s", bucketName, "ap-southeast-1", key);
        } catch (S3Exception e) {
            throw new RuntimeException("Failed to upload file to S3: " + e.getMessage(), e);
        }
    }
}
