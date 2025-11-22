package com.docmanagement.controller;

import com.docmanagement.service.RagService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/rag")
@RequiredArgsConstructor
public class RagController {
    
    private final RagService ragService;
    
    @PostMapping("/ask")
    public ResponseEntity<?> askQuestion(@RequestBody Map<String, String> request) {
        try {
            String question = request.get("question");
            Integer topK = request.containsKey("topK") ? 
                    Integer.parseInt(request.get("topK")) : 5;
            
            if (question == null || question.trim().isEmpty()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Question is required");
                return ResponseEntity.badRequest().body(error);
            }
            
            Map<String, Object> response = ragService.answerQuestionWithSources(question, topK);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to answer question: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    @PostMapping("/answer")
    public ResponseEntity<?> getAnswer(@RequestBody Map<String, String> request) {
        try {
            String question = request.get("question");
            Integer topK = request.containsKey("topK") ? 
                    Integer.parseInt(request.get("topK")) : 5;
            
            if (question == null || question.trim().isEmpty()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Question is required");
                return ResponseEntity.badRequest().body(error);
            }
            
            String answer = ragService.answerQuestion(question, topK);
            
            Map<String, String> response = new HashMap<>();
            response.put("question", question);
            response.put("answer", answer);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to answer question: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
}

