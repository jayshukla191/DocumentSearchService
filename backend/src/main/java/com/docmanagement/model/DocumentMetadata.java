package com.docmanagement.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * Document metadata stored in DynamoDB
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentMetadata {
    
    private String documentId;
    private List<String> tags;
    private String extractedTextPreview;
    private Long fileSize;
    private Map<String, String> customFields;
}

