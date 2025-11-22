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
@Table(name = "documents")
public class Document {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 500)
    private String filename;
    
    @Column(name = "s3_key", nullable = false, length = 1000)
    private String s3Key;
    
    @Column(name = "content_type", length = 100)
    private String contentType;
    
    @Column(name = "file_size")
    private Long fileSize;
    
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    @Column(name = "upload_date")
    private LocalDateTime uploadDate;
    
    @PrePersist
    protected void onCreate() {
        if (uploadDate == null) {
            uploadDate = LocalDateTime.now();
        }
    }
}

