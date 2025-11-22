package com.docmanagement.config;

import com.pgvector.PGvector;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class PGvectorConverter implements AttributeConverter<PGvector, String> {
    
    @Override
    public String convertToDatabaseColumn(PGvector pgvector) {
        if (pgvector == null) {
            return null;
        }
        // Convert PGvector to string format "[1.0,2.0,3.0]" expected by pgvector
        // PGvector has a toArray() method to get the float array
        float[] values = pgvector.toArray();
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < values.length; i++) {
            sb.append(values[i]);
            if (i < values.length - 1) {
                sb.append(",");
            }
        }
        sb.append("]");
        return sb.toString();
    }
    
    @Override
    public PGvector convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isEmpty()) {
            return null;
        }
        // Parse string format "[1.0,2.0,3.0]" back to PGvector
        // Remove brackets and split by comma
        String cleaned = dbData.trim();
        if (cleaned.startsWith("[") && cleaned.endsWith("]")) {
            cleaned = cleaned.substring(1, cleaned.length() - 1);
        }
        String[] parts = cleaned.split(",");
        float[] values = new float[parts.length];
        for (int i = 0; i < parts.length; i++) {
            values[i] = Float.parseFloat(parts[i].trim());
        }
        return new PGvector(values);
    }
}

