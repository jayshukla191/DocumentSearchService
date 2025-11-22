package com.docmanagement.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.bedrockruntime.BedrockRuntimeClient;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.s3.S3Client;

@Configuration
public class AwsConfig {
    
    @Value("${aws.region}")
    private String region;
    
    @Value("${aws.access-key-id}")
    private String accessKeyId;
    
    @Value("${aws.secret-access-key}")
    private String secretAccessKey;
    
    @Bean
    public S3Client s3Client() {
        AwsBasicCredentials awsCreds = AwsBasicCredentials.create(accessKeyId, secretAccessKey);
        // S3 Express One Zone buckets are automatically detected by the SDK
        // The SDK will use the correct endpoint based on the bucket name pattern
        return S3Client.builder()
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(awsCreds))
                // Force path-style addressing for S3 Express compatibility
                .forcePathStyle(false) // S3 Express uses virtual-hosted style
                .build();
    }
    
    @Bean
    public DynamoDbClient dynamoDbClient() {
        AwsBasicCredentials awsCreds = AwsBasicCredentials.create(accessKeyId, secretAccessKey);
        return DynamoDbClient.builder()
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(awsCreds))
                .build();
    }
    
    @Bean
    public BedrockRuntimeClient bedrockRuntimeClient() {
        AwsBasicCredentials awsCreds = AwsBasicCredentials.create(accessKeyId, secretAccessKey);
        return BedrockRuntimeClient.builder()
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(awsCreds))
                .build();
    }
}

