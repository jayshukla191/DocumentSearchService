package com.docmanagement.service;

import com.docmanagement.model.DocumentMetadata;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class DynamoDbService {
    
    private final DynamoDbClient dynamoDbClient;
    
    @Value("${aws.dynamodb.table-name}")
    private String tableName;
    
    public void saveMetadata(DocumentMetadata metadata) {
        try {
            Map<String, AttributeValue> item = new HashMap<>();
            item.put("DocumentId", AttributeValue.builder().s(metadata.getDocumentId()).build());
            
            if (metadata.getTags() != null && !metadata.getTags().isEmpty()) {
                item.put("Tags", AttributeValue.builder().ss(metadata.getTags()).build());
            }
            
            if (metadata.getExtractedTextPreview() != null) {
                item.put("ExtractedTextPreview", AttributeValue.builder().s(metadata.getExtractedTextPreview()).build());
            }
            
            if (metadata.getFileSize() != null) {
                item.put("FileSize", AttributeValue.builder().n(metadata.getFileSize().toString()).build());
            }
            
            if (metadata.getCustomFields() != null) {
                metadata.getCustomFields().forEach((key, value) -> 
                    item.put(key, AttributeValue.builder().s(value).build())
                );
            }
            
            PutItemRequest request = PutItemRequest.builder()
                    .tableName(tableName)
                    .item(item)
                    .build();
            
            dynamoDbClient.putItem(request);
            log.info("Metadata saved successfully for document: {}", metadata.getDocumentId());
        } catch (Exception e) {
            log.error("Error saving metadata to DynamoDB", e);
            throw new RuntimeException("Failed to save metadata: " + e.getMessage());
        }
    }
    
    public DocumentMetadata getMetadata(String documentId) {
        try {
            Map<String, AttributeValue> key = new HashMap<>();
            key.put("DocumentId", AttributeValue.builder().s(documentId).build());
            
            GetItemRequest request = GetItemRequest.builder()
                    .tableName(tableName)
                    .key(key)
                    .build();
            
            GetItemResponse response = dynamoDbClient.getItem(request);
            
            if (response.item() == null || response.item().isEmpty()) {
                return null;
            }
            
            Map<String, AttributeValue> item = response.item();
            
            DocumentMetadata metadata = new DocumentMetadata();
            metadata.setDocumentId(documentId);
            
            if (item.containsKey("Tags")) {
                metadata.setTags(item.get("Tags").ss());
            }
            
            if (item.containsKey("ExtractedTextPreview")) {
                metadata.setExtractedTextPreview(item.get("ExtractedTextPreview").s());
            }
            
            if (item.containsKey("FileSize")) {
                metadata.setFileSize(Long.parseLong(item.get("FileSize").n()));
            }
            
            return metadata;
        } catch (Exception e) {
            log.error("Error getting metadata from DynamoDB", e);
            throw new RuntimeException("Failed to get metadata: " + e.getMessage());
        }
    }
    
    public void deleteMetadata(String documentId) {
        try {
            Map<String, AttributeValue> key = new HashMap<>();
            key.put("DocumentId", AttributeValue.builder().s(documentId).build());
            
            DeleteItemRequest request = DeleteItemRequest.builder()
                    .tableName(tableName)
                    .key(key)
                    .build();
            
            dynamoDbClient.deleteItem(request);
            log.info("Metadata deleted successfully for document: {}", documentId);
        } catch (Exception e) {
            log.error("Error deleting metadata from DynamoDB", e);
            throw new RuntimeException("Failed to delete metadata: " + e.getMessage());
        }
    }
}

