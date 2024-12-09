package com.example.sharingfood;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

@Configuration
public class AwsConfig {

    @Bean
    public S3Client s3Client() {
        return S3Client.builder()
                .region(Region.AP_SOUTHEAST_1) // 根據您的 S3 Bucket 的區域
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(
                                "AKIAZOZQFJHQPSHRRQBY", // 您的 Access Key ID
                                "JksIoaL+MhCbTTlw5nY2QSO8r/TxCpT8MtY1fjcd" // 您的 Secret Access Key
                        )
                ))
                .build();
    }
}
