package com.docmanagement.repository;

import com.docmanagement.model.Document;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Long> {
    
    List<Document> findByUserIdOrderByUploadDateDesc(Long userId);
    
    List<Document> findByUserId(Long userId);
    
    long countByUserId(Long userId);
}

