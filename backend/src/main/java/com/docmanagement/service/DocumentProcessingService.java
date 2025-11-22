package com.docmanagement.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class DocumentProcessingService {
    
    private static final int CHUNK_SIZE = 500; // words per chunk
    
    /**
     * Extract text from PDF file
     */
    public String extractTextFromPdf(byte[] fileBytes) throws IOException {
        try (PDDocument document = Loader.loadPDF(fileBytes)) {
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(document);
            log.info("Extracted {} characters from PDF", text.length());
            return text;
        } catch (Exception e) {
            log.error("Error extracting text from PDF", e);
            throw new IOException("Failed to extract text from PDF: " + e.getMessage());
        }
    }
    
    /**
     * Extract text from image using Tesseract OCR
     */
    public String extractTextFromImage(byte[] fileBytes) throws IOException {
        try {
            BufferedImage image = ImageIO.read(new ByteArrayInputStream(fileBytes));
            Tesseract tesseract = new Tesseract();
            // Set tessdata path if needed: tesseract.setDatapath("/path/to/tessdata");
            String text = tesseract.doOCR(image);
            log.info("Extracted {} characters from image using OCR", text.length());
            return text;
        } catch (TesseractException e) {
            log.error("Error performing OCR on image", e);
            throw new IOException("Failed to perform OCR on image: " + e.getMessage());
        }
    }
    
    /**
     * Extract text based on content type
     */
    public String extractText(byte[] fileBytes, String contentType) throws IOException {
        if (contentType == null) {
            throw new IOException("Content type is null");
        }
        
        if (contentType.equals("application/pdf")) {
            return extractTextFromPdf(fileBytes);
        } else if (contentType.startsWith("image/")) {
            return extractTextFromImage(fileBytes);
        } else if (contentType.equals("text/plain")) {
            return new String(fileBytes);
        } else {
            throw new IOException("Unsupported content type: " + contentType);
        }
    }
    
    /**
     * Chunk text into smaller segments for embedding
     */
    public List<String> chunkText(String text) {
        List<String> chunks = new ArrayList<>();
        
        if (text == null || text.trim().isEmpty()) {
            return chunks;
        }
        
        String[] words = text.split("\\s+");
        StringBuilder currentChunk = new StringBuilder();
        int wordCount = 0;
        
        for (String word : words) {
            currentChunk.append(word).append(" ");
            wordCount++;
            
            if (wordCount >= CHUNK_SIZE) {
                chunks.add(currentChunk.toString().trim());
                currentChunk = new StringBuilder();
                wordCount = 0;
            }
        }
        
        // Add the last chunk if it's not empty
        if (currentChunk.length() > 0) {
            chunks.add(currentChunk.toString().trim());
        }
        
        log.info("Created {} chunks from text", chunks.size());
        return chunks;
    }
    
    /**
     * Get preview text (first 500 characters)
     */
    public String getTextPreview(String text) {
        if (text == null || text.isEmpty()) {
            return "";
        }
        return text.substring(0, Math.min(500, text.length()));
    }
}

