package com.docmanagement.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "document_chunks")
public class DocumentChunk {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "document_id", nullable = false)
    private Long documentId;
    
    @Column(name = "chunk_text", nullable = false, columnDefinition = "TEXT")
    private String chunkText;
    
    @Column(name = "chunk_index", nullable = false)
    private Integer chunkIndex;
    
    @Column(name = "embedding", columnDefinition = "vector(1024)")
    private String embedding; // Store as String in format "[1.0,2.0,3.0]" - Titan V2 uses 1024 dimensions
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}

