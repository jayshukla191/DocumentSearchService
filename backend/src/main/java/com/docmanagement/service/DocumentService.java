package com.docmanagement.service;

import com.docmanagement.model.Document;
import com.docmanagement.model.DocumentMetadata;
import com.docmanagement.repository.DocumentRepository;
import com.docmanagement.repository.VectorRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class DocumentService {
    
    private final S3Service s3Service;
    private final DocumentRepository documentRepository;
    private final DocumentProcessingService documentProcessingService;
    private final ChunkProcessingService chunkProcessingService;
    private final VectorRepository vectorRepository;
    private final DynamoDbService dynamoDbService;
    
    /**
     * Process and store a document with full pipeline:
     * 1. Upload to S3
     * 2. Save document entity
     * 3. Extract text
     * 4. Chunk text
     * 5. Generate embeddings
     * 6. Store embeddings
     * 7. Save metadata to DynamoDB
     */
    public Document uploadDocument(MultipartFile file, Long userId) throws IOException {
        log.info("Starting document upload for user: {}", userId);
        
        // Read file bytes ONCE before any processing (stream can only be read once)
        byte[] fileBytes = file.getBytes();
        log.info("Read {} bytes from file", fileBytes.length);
        
        // 1. Upload to S3 (using the bytes we already read)
        String s3Key = s3Service.uploadFile(fileBytes, file.getOriginalFilename(), file.getContentType(), userId);
        
        // 2. Save document entity (in a separate transaction to ensure it's saved even if processing fails)
        Document document = saveDocumentEntity(file, s3Key, userId);
        
        log.info("Document saved with ID: {}", document.getId());
        
        // 3. Extract text (asynchronously in production)
        // Process in a separate try-catch so document is always saved
        try {
            log.info("=== STEP 3: Extracting text from document ===");
            log.info("Content type: {}, File size: {} bytes", file.getContentType(), fileBytes.length);
            String extractedText = null;
            try {
                extractedText = documentProcessingService.extractText(fileBytes, file.getContentType());
                log.info("Successfully extracted {} characters of text", extractedText.length());
                log.debug("Text preview (first 200 chars): {}", extractedText.substring(0, Math.min(200, extractedText.length())));
            } catch (Exception e) {
                log.error("FAILED at Step 3 (Text Extraction)", e);
                log.error("Exception type: {}, Message: {}", e.getClass().getName(), e.getMessage());
                e.printStackTrace();
                throw e; // Re-throw to be caught by outer catch
            }
            
            // 4. Chunk text
            log.info("=== STEP 4: Chunking text ===");
            List<String> chunks = null;
            try {
                chunks = documentProcessingService.chunkText(extractedText);
                log.info("Successfully created {} chunks", chunks.size());
                for (int i = 0; i < chunks.size(); i++) {
                    log.debug("Chunk {}: {} characters", i, chunks.get(i).length());
                }
            } catch (Exception e) {
                log.error("FAILED at Step 4 (Chunking)", e);
                log.error("Exception type: {}, Message: {}", e.getClass().getName(), e.getMessage());
                e.printStackTrace();
                throw e; // Re-throw to be caught by outer catch
            }
            
            // 5 & 6. Generate embeddings and store
            log.info("=== STEP 5-6: Generating embeddings and storing chunks ===");
            log.info("Processing {} chunks for document ID: {}", chunks.size(), document.getId());
            try {
                // Call separate service - this ensures @Transactional works properly (avoids self-invocation issue)
                chunkProcessingService.processChunks(document.getId(), chunks);
                log.info("Successfully processed {} chunks", chunks.size());
            } catch (Exception e) {
                log.error("FAILED at Step 5-6 (Embedding Generation/Storage)", e);
                log.error("Exception type: {}, Message: {}", e.getClass().getName(), e.getMessage());
                e.printStackTrace();
                throw e; // Re-throw to be caught by outer catch
            }
            
            // 7. Save metadata to DynamoDB
            log.info("=== STEP 7: Saving metadata to DynamoDB ===");
            try {
                DocumentMetadata metadata = DocumentMetadata.builder()
                        .documentId(document.getId().toString())
                        .extractedTextPreview(documentProcessingService.getTextPreview(extractedText))
                        .fileSize(file.getSize())
                        .tags(new ArrayList<>())
                        .build();
                dynamoDbService.saveMetadata(metadata);
                log.info("Metadata saved to DynamoDB");
            } catch (Exception e) {
                log.error("FAILED at Step 7 (DynamoDB Metadata)", e);
                log.error("Exception type: {}, Message: {}", e.getClass().getName(), e.getMessage());
                e.printStackTrace();
                // Don't re-throw - metadata is optional, document is already processed
            }
            
            log.info("Document processing completed successfully for ID: {}", document.getId());
        } catch (Exception e) {
            log.error("CRITICAL ERROR: Document processing failed for document ID: {}", document.getId());
            log.error("Exception class: {}", e.getClass().getName());
            log.error("Exception message: {}", e.getMessage());
            log.error("Exception cause: {}", e.getCause() != null ? e.getCause().getMessage() : "N/A");
            log.error("Full stack trace:", e);
            e.printStackTrace();
            // Document is still saved even if processing fails
        }
        
        return document;
    }
    
    /**
     * Save document entity in a transaction
     * Must be public for @Transactional to work with Spring AOP proxies
     */
    @Transactional
    public Document saveDocumentEntity(MultipartFile file, String s3Key, Long userId) {
        Document document = Document.builder()
                .filename(file.getOriginalFilename())
                .s3Key(s3Key)
                .contentType(file.getContentType())
                .fileSize(file.getSize())
                .userId(userId)
                .build();
        return documentRepository.save(document);
    }
    
    /**
     * Get all documents for a user
     */
    public List<Document> getUserDocuments(Long userId) {
        return documentRepository.findByUserIdOrderByUploadDateDesc(userId);
    }
    
    /**
     * Get document by ID
     */
    public Document getDocumentById(Long documentId) {
        return documentRepository.findById(documentId)
                .orElseThrow(() -> new RuntimeException("Document not found"));
    }
    
    /**
     * Delete document and all related data
     */
    @Transactional
    public void deleteDocument(Long documentId) {
        Document document = getDocumentById(documentId);
        
        // Delete from S3
        s3Service.deleteFile(document.getS3Key());
        
        // Delete chunks from vector database
        vectorRepository.deleteByDocumentId(documentId);
        
        // Delete metadata from DynamoDB
        dynamoDbService.deleteMetadata(documentId.toString());
        
        // Delete document entity
        documentRepository.delete(document);
        
        log.info("Document deleted: {}", documentId);
    }
    
    /**
     * Download document file
     */
    public byte[] downloadDocument(Long documentId) {
        Document document = getDocumentById(documentId);
        return s3Service.downloadFile(document.getS3Key());
    }
}

