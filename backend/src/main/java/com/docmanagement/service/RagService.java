package com.docmanagement.service;

import com.docmanagement.model.DocumentChunk;
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
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RagService {
    
    private final SemanticSearchService semanticSearchService;
    private final BedrockRuntimeClient bedrockClient;
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    @Value("${aws.bedrock.llm-model-id}")
    private String llmModelId;
    
    /**
     * Answer a question using RAG (Retrieval-Augmented Generation)
     */
    public String answerQuestion(String question, int topK) {
        try {
            // Step 1: Retrieve relevant document chunks
            List<DocumentChunk> relevantChunks = semanticSearchService.search(question, topK);
            
            if (relevantChunks.isEmpty()) {
                return "I couldn't find any relevant information in the documents to answer your question.";
            }
            
            // Step 2: Build context from chunks
            String context = buildContext(relevantChunks);
            
            // Step 3: Generate answer using LLM
            String answer = generateAnswer(question, context);
            
            log.info("Generated answer for question: {}", question);
            return answer;
            
        } catch (Exception e) {
            log.error("Error in RAG question answering", e);
            throw new RuntimeException("Failed to answer question: " + e.getMessage());
        }
    }
    
    /**
     * Build context from retrieved chunks
     */
    private String buildContext(List<DocumentChunk> chunks) {
        return chunks.stream()
                .map(DocumentChunk::getChunkText)
                .collect(Collectors.joining("\n\n"));
    }
    
    /**
     * Generate answer using AWS Bedrock LLM
     */
    private String generateAnswer(String question, String context) {
        try {
            // Build prompt with context
            String prompt = String.format(
                    "Based on the following context, please answer the question. " +
                    "If the answer cannot be found in the context, say so.\n\n" +
                    "Context:\n%s\n\n" +
                    "Question: %s\n\n" +
                    "Answer:",
                    context, question
            );
            
            // Prepare request for Titan Text model
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("inputText", prompt);
            
            Map<String, Object> textGenerationConfig = new HashMap<>();
            textGenerationConfig.put("maxTokenCount", 512);
            textGenerationConfig.put("temperature", 0.7);
            textGenerationConfig.put("topP", 0.9);
            requestBody.put("textGenerationConfig", textGenerationConfig);
            
            String jsonBody = objectMapper.writeValueAsString(requestBody);
            
            InvokeModelRequest request = InvokeModelRequest.builder()
                    .modelId(llmModelId)
                    .contentType("application/json")
                    .accept("application/json")
                    .body(SdkBytes.fromUtf8String(jsonBody))
                    .build();
            
            InvokeModelResponse response = bedrockClient.invokeModel(request);
            
            // Parse response
            String responseBody = response.body().asUtf8String();
            Map<String, Object> responseMap = objectMapper.readValue(responseBody, Map.class);
            
            // Extract generated text
            List<Map<String, Object>> results = (List<Map<String, Object>>) responseMap.get("results");
            if (results != null && !results.isEmpty()) {
                return (String) results.get(0).get("outputText");
            }
            
            return "Unable to generate answer.";
            
        } catch (Exception e) {
            log.error("Error generating answer with Bedrock", e);
            throw new RuntimeException("Failed to generate answer: " + e.getMessage());
        }
    }
    
    /**
     * Get RAG response with source documents
     */
    public Map<String, Object> answerQuestionWithSources(String question, int topK) {
        List<DocumentChunk> relevantChunks = semanticSearchService.search(question, topK);
        String answer = relevantChunks.isEmpty() ? 
                "I couldn't find any relevant information in the documents to answer your question." :
                generateAnswer(question, buildContext(relevantChunks));
        
        Map<String, Object> response = new HashMap<>();
        response.put("answer", answer);
        response.put("sources", relevantChunks);
        response.put("sourceCount", relevantChunks.size());
        
        return response;
    }
}

