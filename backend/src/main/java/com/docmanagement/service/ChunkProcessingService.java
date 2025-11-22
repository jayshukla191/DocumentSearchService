package com.docmanagement.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service responsible for processing document chunks and storing embeddings
 * Separated to ensure proper transaction management via Spring AOP
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ChunkProcessingService {
    
    private final EmbeddingService embeddingService;
    
    @PersistenceContext
    private EntityManager entityManager;
    
    /**
     * Process chunks: generate embeddings and store in vector database
     * This is in a separate service to ensure @Transactional works properly
     * (avoids Spring AOP self-invocation issues)
     */
    @Transactional
    public void processChunks(Long documentId, List<String> chunks) {
        log.info("Starting to process {} chunks for document {}", chunks.size(), documentId);
        
        for (int i = 0; i < chunks.size(); i++) {
            String chunkText = chunks.get(i);
            log.info("Processing chunk {}/{}: {} characters", i + 1, chunks.size(), chunkText.length());
            
            try {
                // Generate embedding
                log.debug("Generating embedding for chunk {}...", i);
                float[] embedding = null;
                try {
                    embedding = embeddingService.generateEmbedding(chunkText);
                    log.info("Generated embedding for chunk {}: {} dimensions", i, embedding.length);
                } catch (Exception e) {
                    log.error("FAILED to generate embedding for chunk {}", i, e);
                    log.error("Chunk text preview (first 100 chars): {}", 
                             chunkText.substring(0, Math.min(100, chunkText.length())));
                    throw new RuntimeException("Failed to generate embedding for chunk " + i, e);
                }
                
                // Convert embedding to string format for pgvector
                String embeddingString = null;
                try {
                    embeddingString = embeddingService.embeddingToString(embedding);
                    log.debug("Converted embedding to string format for chunk {}", i);
                } catch (Exception e) {
                    log.error("FAILED to convert embedding to string for chunk {}", i, e);
                    throw new RuntimeException("Failed to convert embedding to string for chunk " + i, e);
                }
                
                // Save chunk with embedding using native SQL with CAST via EntityManager
                log.debug("Saving chunk {} to database...", i);
                try {
                    String sql = "INSERT INTO document_chunks (document_id, chunk_text, chunk_index, embedding, created_at) " +
                                "VALUES (:documentId, :chunkText, :chunkIndex, CAST(:embedding AS vector), CURRENT_TIMESTAMP)";
                    
                    // EntityManager automatically participates in Spring transaction when method is @Transactional
                    int rowsAffected = entityManager.createNativeQuery(sql)
                            .setParameter("documentId", documentId)
                            .setParameter("chunkText", chunkText)
                            .setParameter("chunkIndex", i)
                            .setParameter("embedding", embeddingString)
                            .executeUpdate();
                    
                    // Flush to ensure the insert is executed immediately
                    entityManager.flush();
                    
                    if (rowsAffected > 0) {
                        log.info("Successfully saved chunk {}/{} for document {} ({} rows affected)", 
                                i + 1, chunks.size(), documentId, rowsAffected);
                    } else {
                        log.warn("No rows affected when saving chunk {} for document {}", i, documentId);
                        throw new RuntimeException("Failed to save chunk " + i + " - no rows affected");
                    }
                } catch (Exception e) {
                    log.error("FAILED to save chunk {} to database", i, e);
                    log.error("Exception type: {}, Message: {}", e.getClass().getName(), e.getMessage());
                    log.error("Document ID: {}, Chunk index: {}, Chunk text length: {}, Embedding length: {}", 
                             documentId, i, chunkText.length(), embeddingString.length());
                    log.error("Embedding string preview (first 100 chars): {}", 
                             embeddingString.substring(0, Math.min(100, embeddingString.length())));
                    e.printStackTrace();
                    throw new RuntimeException("Failed to save chunk " + i + " to database", e);
                }
            } catch (Exception e) {
                log.error("CRITICAL: Error processing chunk {}/{} for document {}", i + 1, chunks.size(), documentId, e);
                log.error("This will cause the entire transaction to rollback");
                throw e; // Re-throw to fail the transaction
            }
        }
        
        log.info("Successfully processed all {} chunks for document {}", chunks.size(), documentId);
    }
}

