package com.docmanagement.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.IOException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class S3Service {
    
    private final S3Client s3Client;
    
    @Value("${aws.s3.bucket-name}")
    private String bucketName;
    
    public String uploadFile(MultipartFile file, Long userId) throws IOException {
        return uploadFile(file.getBytes(), file.getOriginalFilename(), file.getContentType(), userId);
    }
    
    public String uploadFile(byte[] fileBytes, String fileName, String contentType, Long userId) throws IOException {
        String fileExtension = fileName.substring(fileName.lastIndexOf("."));
        String uniqueFileName = UUID.randomUUID().toString() + fileExtension;
        String s3Key = String.format("documents/user-%d/%s", userId, uniqueFileName);
        
        try {
            log.info("Attempting to upload file to S3 bucket: {}, key: {}", bucketName, s3Key);
            
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(s3Key)
                    .contentType(contentType)
                    .contentLength((long) fileBytes.length)
                    .build();
            
            s3Client.putObject(putObjectRequest, RequestBody.fromBytes(fileBytes));
            
            log.info("File uploaded successfully to S3: {}", s3Key);
            return s3Key;
        } catch (software.amazon.awssdk.services.s3.model.NoSuchBucketException e) {
            log.error("S3 bucket does not exist: {}", bucketName, e);
            throw new RuntimeException("S3 bucket does not exist or is not accessible: " + bucketName + ". Please verify the bucket name and AWS credentials.");
        } catch (software.amazon.awssdk.services.s3.model.S3Exception e) {
            log.error("S3 error: Status Code: {}, Error Code: {}, Message: {}", e.statusCode(), e.awsErrorDetails().errorCode(), e.getMessage(), e);
            throw new RuntimeException("S3 error: " + e.awsErrorDetails().errorCode() + " - " + e.getMessage());
        } catch (Exception e) {
            log.error("Error uploading file to S3", e);
            throw new RuntimeException("Failed to upload file to S3: " + e.getMessage());
        }
    }
    
    public byte[] downloadFile(String s3Key) {
        try {
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(s3Key)
                    .build();
            
            return s3Client.getObjectAsBytes(getObjectRequest).asByteArray();
        } catch (Exception e) {
            log.error("Error downloading file from S3: {}", s3Key, e);
            throw new RuntimeException("Failed to download file from S3: " + e.getMessage());
        }
    }
    
    public String getPresignedUrl(String s3Key, int expirationMinutes) {
        // For simplicity, we'll use GetObject
        // In production, you might want to use presigned URLs for better security
        return String.format("https://%s.s3.amazonaws.com/%s", bucketName, s3Key);
    }
    
    public void deleteFile(String s3Key) {
        try {
            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(s3Key)
                    .build();
            
            s3Client.deleteObject(deleteObjectRequest);
            log.info("File deleted successfully from S3: {}", s3Key);
        } catch (Exception e) {
            log.error("Error deleting file from S3: {}", s3Key, e);
            throw new RuntimeException("Failed to delete file from S3: " + e.getMessage());
        }
    }
}

