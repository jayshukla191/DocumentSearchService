package com.docmanagement.repository;

import com.docmanagement.model.DocumentChunk;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VectorRepository extends JpaRepository<DocumentChunk, Long> {
    
    List<DocumentChunk> findByDocumentId(Long documentId);
    
    void deleteByDocumentId(Long documentId);
    
    /**
     * Insert a document chunk with vector embedding using native SQL
     * This uses CAST to properly convert the string to vector type
     */
    @Modifying
    @Query(value = "INSERT INTO document_chunks (document_id, chunk_text, chunk_index, embedding, created_at) " +
                   "VALUES (:documentId, :chunkText, :chunkIndex, CAST(:embedding AS vector), CURRENT_TIMESTAMP)",
           nativeQuery = true)
    int insertChunkWithEmbedding(@Param("documentId") Long documentId,
                                  @Param("chunkText") String chunkText,
                                  @Param("chunkIndex") Integer chunkIndex,
                                  @Param("embedding") String embedding);
    
    /**
     * Find similar document chunks using cosine similarity with pgvector
     * The <-> operator calculates cosine distance (1 - cosine similarity)
     * Lower distance means higher similarity
     */
    @Query(value = "SELECT * FROM document_chunks " +
                   "ORDER BY embedding <-> CAST(:embedding AS vector) " +
                   "LIMIT :limit", 
           nativeQuery = true)
    List<DocumentChunk> findSimilarChunks(@Param("embedding") String embedding, 
                                          @Param("limit") int limit);
    
    /**
     * Find similar chunks with a minimum similarity threshold
     */
    @Query(value = "SELECT *, (1 - (embedding <-> CAST(:embedding AS vector))) as similarity " +
                   "FROM document_chunks " +
                   "WHERE (1 - (embedding <-> CAST(:embedding AS vector))) > :threshold " +
                   "ORDER BY embedding <-> CAST(:embedding AS vector) " +
                   "LIMIT :limit", 
           nativeQuery = true)
    List<Object[]> findSimilarChunksWithScore(@Param("embedding") String embedding, 
                                               @Param("threshold") double threshold,
                                               @Param("limit") int limit);
}

