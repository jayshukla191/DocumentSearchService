package com.docmanagement.controller;

import com.docmanagement.model.Document;
import com.docmanagement.model.User;
import com.docmanagement.service.DocumentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
public class DocumentController {
    
    private final DocumentService documentService;
    
    @PostMapping("/upload")
    public ResponseEntity<?> uploadDocument(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "userId", required = false) Long userId,
            Authentication authentication) {
        try {
            // For testing: use userId param if authentication is null
            Long actualUserId = (authentication != null) ? 
                    ((User) authentication.getPrincipal()).getId() : 
                    (userId != null ? userId : 1L);
            Document document = documentService.uploadDocument(file, actualUserId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "File uploaded successfully");
            response.put("documentId", document.getId());
            response.put("filename", document.getFilename());
            response.put("uploadDate", document.getUploadDate());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to upload file: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @GetMapping
    public ResponseEntity<List<Document>> getUserDocuments(
            @RequestParam(value = "userId", required = false) Long userId,
            Authentication authentication) {
        // For testing: use userId param if authentication is null
        Long actualUserId = (authentication != null) ? 
                ((User) authentication.getPrincipal()).getId() : 
                (userId != null ? userId : 1L);
        List<Document> documents = documentService.getUserDocuments(actualUserId);
        return ResponseEntity.ok(documents);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<?> getDocument(
            @PathVariable Long id,
            @RequestParam(value = "userId", required = false) Long userId,
            Authentication authentication) {
        try {
            Document document = documentService.getDocumentById(id);
            
            // For testing: use userId param if authentication is null
            if (authentication != null) {
                User user = (User) authentication.getPrincipal();
                // Check if user owns the document
                if (!document.getUserId().equals(user.getId())) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied");
                }
            } else if (userId != null) {
                // For testing: check userId param
                if (!document.getUserId().equals(userId)) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied");
                }
            }
            
            return ResponseEntity.ok(document);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Document not found");
        }
    }
    
    @GetMapping("/{id}/download")
    public ResponseEntity<?> downloadDocument(
            @PathVariable Long id,
            @RequestParam(value = "userId", required = false) Long userId,
            Authentication authentication) {
        try {
            Document document = documentService.getDocumentById(id);
            
            // For testing: use userId param if authentication is null
            if (authentication != null) {
                User user = (User) authentication.getPrincipal();
                if (!document.getUserId().equals(user.getId())) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied");
                }
            } else if (userId != null) {
                if (!document.getUserId().equals(userId)) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied");
                }
            }
            
            byte[] fileBytes = documentService.downloadDocument(id);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(document.getContentType()));
            headers.setContentDispositionFormData("attachment", document.getFilename());
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(fileBytes);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to download file: " + e.getMessage());
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteDocument(
            @PathVariable Long id,
            @RequestParam(value = "userId", required = false) Long userId,
            Authentication authentication) {
        try {
            Document document = documentService.getDocumentById(id);
            
            // For testing: use userId param if authentication is null
            if (authentication != null) {
                User user = (User) authentication.getPrincipal();
                if (!document.getUserId().equals(user.getId())) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied");
                }
            } else if (userId != null) {
                if (!document.getUserId().equals(userId)) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied");
                }
            }
            
            documentService.deleteDocument(id);
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "Document deleted successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to delete document: " + e.getMessage());
        }
    }
}

