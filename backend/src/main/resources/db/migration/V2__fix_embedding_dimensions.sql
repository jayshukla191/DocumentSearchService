-- Fix embedding dimensions for Titan V2 model
-- Amazon Titan Embed Text V2 produces 1024 dimensions, not 1536

-- Drop the existing index on the embedding column
DROP INDEX IF EXISTS idx_chunks_embedding;

-- Alter the embedding column to use the correct dimensions
ALTER TABLE document_chunks 
ALTER COLUMN embedding TYPE vector(1024);

-- Recreate the HNSW index with the corrected dimensions
CREATE INDEX IF NOT EXISTS idx_chunks_embedding ON document_chunks 
USING hnsw (embedding vector_cosine_ops);

