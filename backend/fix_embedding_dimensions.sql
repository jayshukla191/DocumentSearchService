-- Manual fix for embedding dimensions mismatch
-- Amazon Titan Embed Text V2 produces 1024 dimensions, not 1536
-- Run this SQL directly on your PostgreSQL database

-- Drop the existing index on the embedding column
DROP INDEX IF EXISTS idx_chunks_embedding;

-- Alter the embedding column to use the correct dimensions
ALTER TABLE document_chunks 
ALTER COLUMN embedding TYPE vector(1024);

-- Recreate the HNSW index with the corrected dimensions
CREATE INDEX IF NOT EXISTS idx_chunks_embedding ON document_chunks 
USING hnsw (embedding vector_cosine_ops);

-- Verify the change
SELECT 
    column_name, 
    data_type, 
    udt_name
FROM information_schema.columns 
WHERE table_name = 'document_chunks' 
AND column_name = 'embedding';

-- Show message
SELECT 'Embedding dimensions updated successfully to 1024!' as status;

