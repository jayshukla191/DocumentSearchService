package com.docmanagement.controller;

import com.docmanagement.model.DocumentChunk;
import com.docmanagement.service.SemanticSearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
public class SearchController {
    
    private final SemanticSearchService semanticSearchService;
    
    @GetMapping
    public ResponseEntity<?> search(
            @RequestParam String query,
            @RequestParam(defaultValue = "10") int limit) {
        try {
            List<DocumentChunk> results = semanticSearchService.search(query, limit);
            
            Map<String, Object> response = new HashMap<>();
            response.put("query", query);
            response.put("results", results);
            response.put("count", results.size());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Search failed: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    @GetMapping("/scored")
    public ResponseEntity<?> searchWithScore(
            @RequestParam String query,
            @RequestParam(defaultValue = "0.5") double threshold,
            @RequestParam(defaultValue = "10") int limit) {
        try {
            List<Object[]> results = semanticSearchService.searchWithScore(query, threshold, limit);
            
            Map<String, Object> response = new HashMap<>();
            response.put("query", query);
            response.put("results", results);
            response.put("count", results.size());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Search failed: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
}

