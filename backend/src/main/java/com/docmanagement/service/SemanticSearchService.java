package com.docmanagement.service;

import com.docmanagement.model.DocumentChunk;
import com.docmanagement.repository.VectorRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SemanticSearchService {
    
    private final EmbeddingService embeddingService;
    private final VectorRepository vectorRepository;
    
    /**
     * Perform semantic search using vector similarity
     */
    public List<DocumentChunk> search(String query, int limit) {
        try {
            // Generate embedding for the query
            float[] queryEmbedding = embeddingService.generateEmbedding(query);
            String embeddingString = embeddingService.embeddingToString(queryEmbedding);
            
            // Search for similar chunks
            List<DocumentChunk> results = vectorRepository.findSimilarChunks(embeddingString, limit);
            
            log.info("Found {} similar chunks for query: {}", results.size(), query);
            return results;
            
        } catch (Exception e) {
            log.error("Error performing semantic search", e);
            throw new RuntimeException("Failed to perform semantic search: " + e.getMessage());
        }
    }
    
    /**
     * Search with similarity threshold
     */
    public List<Object[]> searchWithScore(String query, double threshold, int limit) {
        try {
            float[] queryEmbedding = embeddingService.generateEmbedding(query);
            String embeddingString = embeddingService.embeddingToString(queryEmbedding);
            
            return vectorRepository.findSimilarChunksWithScore(embeddingString, threshold, limit);
            
        } catch (Exception e) {
            log.error("Error performing semantic search with score", e);
            throw new RuntimeException("Failed to perform semantic search: " + e.getMessage());
        }
    }
}

