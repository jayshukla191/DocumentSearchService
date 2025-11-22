package com.docmanagement.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.bedrockruntime.BedrockRuntimeClient;
import software.amazon.awssdk.services.bedrockruntime.model.InvokeModelRequest;
import software.amazon.awssdk.services.bedrockruntime.model.InvokeModelResponse;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmbeddingService {
    
    private final BedrockRuntimeClient bedrockClient;
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    @Value("${aws.bedrock.embedding-model-id}")
    private String embeddingModelId;
    
    /**
     * Generate embeddings using AWS Bedrock Titan Embeddings
     */
    public float[] generateEmbedding(String text) {
        try {
            log.debug("Generating embedding for text (length: {}) using model: {}", text.length(), embeddingModelId);
            
            // Prepare request body for Titan embeddings
            // Titan V2 uses "inputText" field, same as V1
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("inputText", text);
            
            String jsonBody = objectMapper.writeValueAsString(requestBody);
            log.debug("Request body: {}", jsonBody);
            
            InvokeModelRequest request = InvokeModelRequest.builder()
                    .modelId(embeddingModelId)
                    .contentType("application/json")
                    .accept("application/json")
                    .body(SdkBytes.fromUtf8String(jsonBody))
                    .build();
            
            log.debug("Invoking Bedrock model: {}", embeddingModelId);
            InvokeModelResponse response = bedrockClient.invokeModel(request);
            
            // Parse response
            String responseBody = response.body().asUtf8String();
            log.debug("Response body length: {}", responseBody.length());
            
            Map<String, Object> responseMap = objectMapper.readValue(responseBody, Map.class);
            
            // Extract embedding array - check for different possible field names
            List<Double> embeddingList = null;
            if (responseMap.containsKey("embedding")) {
                embeddingList = (List<Double>) responseMap.get("embedding");
            } else if (responseMap.containsKey("embeddings")) {
                // Some models return "embeddings" (plural)
                List<List<Double>> embeddingsList = (List<List<Double>>) responseMap.get("embeddings");
                if (embeddingsList != null && !embeddingsList.isEmpty()) {
                    embeddingList = embeddingsList.get(0);
                }
            } else {
                log.error("Response does not contain 'embedding' or 'embeddings' field. Response keys: {}", responseMap.keySet());
                throw new RuntimeException("Invalid response format from Bedrock: missing embedding field");
            }
            
            if (embeddingList == null || embeddingList.isEmpty()) {
                throw new RuntimeException("Empty embedding returned from Bedrock");
            }
            
            float[] embedding = new float[embeddingList.size()];
            for (int i = 0; i < embeddingList.size(); i++) {
                embedding[i] = embeddingList.get(i).floatValue();
            }
            
            log.info("Generated embedding with {} dimensions", embedding.length);
            return embedding;
            
        } catch (Exception e) {
            log.error("Error generating embedding with Bedrock. Model: {}, Error: {}", embeddingModelId, e.getMessage(), e);
            throw new RuntimeException("Failed to generate embedding: " + e.getMessage(), e);
        }
    }
    
    /**
     * Generate embeddings for multiple texts in batch
     */
    public List<float[]> generateEmbeddings(List<String> texts) {
        return texts.stream()
                .map(this::generateEmbedding)
                .toList();
    }
    
    /**
     * Convert float array to string format for pgvector
     */
    public String embeddingToString(float[] embedding) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < embedding.length; i++) {
            sb.append(embedding[i]);
            if (i < embedding.length - 1) {
                sb.append(",");
            }
        }
        sb.append("]");
        return sb.toString();
    }
}

