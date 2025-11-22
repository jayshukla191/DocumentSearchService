# Intelligent Document Management System

A full-stack document management system with AI-powered semantic search and RAG (Retrieval-Augmented Generation) capabilities.

## 🌟 Features

- **Document Upload & Storage**: Upload PDFs, images, and text files to AWS S3
- **Text Extraction**: Automatic text extraction using PDFBox and Tesseract OCR
- **Semantic Search**: AI-powered search by meaning, not just keywords
- **RAG Q&A**: Ask questions about your documents and get AI-generated answers
- **User Authentication**: Secure JWT-based authentication
- **Modern UI**: React-based responsive interface with drag-and-drop upload

## 🏗️ Architecture

### Microservices
- **Backend**: Spring Boot REST API (Port 8080)
- **Frontend**: React SPA (Port 3000)

### Tech Stack

#### Backend
- Java 17
- Spring Boot 3.2.0
- Spring Security with JWT
- PostgreSQL with pgvector
- AWS S3, DynamoDB, Bedrock
- Apache PDFBox, Tesseract OCR

#### Frontend
- React 18
- React Router
- Axios
- React Dropzone

### AWS Services
- **S3**: Document storage
- **DynamoDB**: Metadata storage
- **RDS PostgreSQL with pgvector**: Vector embeddings storage
- **Bedrock Titan**: Embeddings generation and LLM for RAG

## 📋 Prerequisites

### Required Software
- Java 17 or higher
- Node.js 18+ and npm
- PostgreSQL 14+ with pgvector extension
- Maven 3.6+
- AWS Account with access to S3, DynamoDB, and Bedrock
- Tesseract OCR (optional, for image processing)

### AWS Setup
1. Create an S3 bucket (e.g., `document-management-bucket`)
2. Create a DynamoDB table named `DocumentMetadata` with partition key `DocumentId` (String)
3. Enable AWS Bedrock models (Titan Embed Text v1 and Titan Text Lite v1)
4. Configure AWS credentials (via AWS CLI or IAM role)

### Database Setup

```bash
# Install PostgreSQL and pgvector
# On Ubuntu/Debian:
sudo apt-get install postgresql postgresql-contrib

# Install pgvector extension
git clone https://github.com/pgvector/pgvector.git
cd pgvector
make
sudo make install

# Create database
psql -U postgres
CREATE DATABASE document_management;
\c document_management
CREATE EXTENSION vector;
```

## 🚀 Installation & Setup

### 1. Clone the Repository

```bash
git clone <repository-url>
cd DocumentService
```

### 2. Backend Setup

#### Configure application.yml

Edit `backend/src/main/resources/application.yml`:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/document_management
    username: your_postgres_username
    password: your_postgres_password

aws:
  region: us-east-1
  s3:
    bucket-name: your-s3-bucket-name
  dynamodb:
    table-name: DocumentMetadata
  bedrock:
    embedding-model-id: amazon.titan-embed-text-v1
    llm-model-id: amazon.titan-text-lite-v1

jwt:
  secret: your-secret-key-at-least-256-bits-long-change-in-production
  expiration: 86400000  # 24 hours
```

#### Build and Run

```bash
cd backend
mvn clean install
mvn spring-boot:run
```

The backend will start on http://localhost:8080

### 3. Frontend Setup

#### Configure Environment

Create `frontend/.env`:

```env
REACT_APP_API_URL=http://localhost:8080
```

#### Install Dependencies and Run

```bash
cd frontend
npm install
npm start
```

The frontend will start on http://localhost:3000

## 📚 API Documentation

### Authentication Endpoints

#### Register User
```http
POST /api/auth/register
Content-Type: application/json

{
  "username": "john_doe",
  "email": "john@example.com",
  "password": "password123"
}

Response:
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "username": "john_doe",
  "email": "john@example.com",
  "message": "User registered successfully"
}
```

#### Login
```http
POST /api/auth/login
Content-Type: application/json

{
  "username": "john_doe",
  "password": "password123"
}

Response:
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "username": "john_doe",
  "email": "john@example.com",
  "message": "Login successful"
}
```

### Document Endpoints

All document endpoints require authentication. Include JWT token in headers:
```
Authorization: Bearer <your-jwt-token>
```

#### Upload Document
```http
POST /api/documents/upload
Content-Type: multipart/form-data

file: <binary-file-data>

Response:
{
  "message": "File uploaded successfully",
  "documentId": 1,
  "filename": "report.pdf",
  "uploadDate": "2024-01-15T10:30:00"
}
```

#### Get All Documents
```http
GET /api/documents
Authorization: Bearer <token>

Response:
[
  {
    "id": 1,
    "filename": "report.pdf",
    "s3Key": "documents/user-1/abc123.pdf",
    "contentType": "application/pdf",
    "fileSize": 2621440,
    "userId": 1,
    "uploadDate": "2024-01-15T10:30:00"
  }
]
```

#### Download Document
```http
GET /api/documents/{id}/download
Authorization: Bearer <token>

Response: Binary file data
```

#### Delete Document
```http
DELETE /api/documents/{id}
Authorization: Bearer <token>

Response:
{
  "message": "Document deleted successfully"
}
```

### Search Endpoints

#### Semantic Search
```http
GET /api/search?query=safety procedures&limit=10
Authorization: Bearer <token>

Response:
{
  "query": "safety procedures",
  "results": [
    {
      "id": 1,
      "documentId": 1,
      "chunkText": "Safety procedures must be followed at all times...",
      "chunkIndex": 0
    }
  ],
  "count": 10
}
```

#### Search with Similarity Score
```http
GET /api/search/scored?query=budget&threshold=0.5&limit=10
Authorization: Bearer <token>

Response:
{
  "query": "budget",
  "results": [
    {
      "chunk": { ... },
      "similarity": 0.85
    }
  ],
  "count": 5
}
```

### RAG (Q&A) Endpoints

#### Ask Question
```http
POST /api/rag/ask
Authorization: Bearer <token>
Content-Type: application/json

{
  "question": "What are the safety requirements?",
  "topK": "5"
}

Response:
{
  "answer": "Based on the documents, the safety requirements include...",
  "sources": [
    {
      "id": 1,
      "documentId": 1,
      "chunkText": "Safety procedures must...",
      "chunkIndex": 0
    }
  ],
  "sourceCount": 5
}
```

#### Get Simple Answer
```http
POST /api/rag/answer
Authorization: Bearer <token>
Content-Type: application/json

{
  "question": "What is the budget allocation?",
  "topK": "5"
}

Response:
{
  "question": "What is the budget allocation?",
  "answer": "The budget allocation is distributed across..."
}
```

## 🔧 Configuration

### AWS Credentials

Set up AWS credentials using one of these methods:

1. **AWS CLI** (recommended for development):
```bash
aws configure
```

2. **Environment Variables**:
```bash
export AWS_ACCESS_KEY_ID=your_access_key
export AWS_SECRET_ACCESS_KEY=your_secret_key
export AWS_REGION=us-east-1
```

3. **IAM Role** (recommended for production on EC2/ECS)

### Tesseract OCR

For image OCR support, install Tesseract:

**Ubuntu/Debian:**
```bash
sudo apt-get install tesseract-ocr
```

**macOS:**
```bash
brew install tesseract
```

**Windows:**
Download installer from: https://github.com/UB-Mannheim/tesseract/wiki

## 🎯 Usage Guide

### 1. Register and Login
- Navigate to http://localhost:3000
- Click "Register" and create an account
- Login with your credentials

### 2. Upload Documents
- Click on the upload area or drag and drop files
- Supported formats: PDF, images (PNG, JPG), text files
- Files are automatically processed and indexed

### 3. Search Documents
- Use the semantic search bar to find documents by meaning
- Try queries like "budget information" or "safety procedures"
- Results show relevant chunks from your documents

### 4. Ask Questions (RAG)
- Navigate to the Q&A page
- Ask questions about your documents
- Get AI-generated answers with source citations

## 📊 Database Schema

### PostgreSQL Tables

```sql
-- Users table
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(255) UNIQUE NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL DEFAULT 'USER',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Documents table
CREATE TABLE documents (
    id BIGSERIAL PRIMARY KEY,
    filename VARCHAR(500) NOT NULL,
    s3_key VARCHAR(1000) NOT NULL,
    content_type VARCHAR(100),
    file_size BIGINT,
    user_id BIGINT NOT NULL,
    upload_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Document chunks with vector embeddings
CREATE TABLE document_chunks (
    id BIGSERIAL PRIMARY KEY,
    document_id BIGINT NOT NULL,
    chunk_text TEXT NOT NULL,
    chunk_index INTEGER NOT NULL,
    embedding vector(1536),  -- Titan embeddings
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (document_id) REFERENCES documents(id) ON DELETE CASCADE
);

-- Create HNSW index for fast vector similarity search
CREATE INDEX idx_chunks_embedding ON document_chunks 
USING hnsw (embedding vector_cosine_ops);
```

### DynamoDB Schema

**Table Name**: DocumentMetadata

**Partition Key**: DocumentId (String)

**Attributes**:
- Tags (String Set)
- ExtractedTextPreview (String)
- FileSize (Number)
- Custom fields as needed

## 🔐 Security

- JWT tokens expire after 24 hours (configurable)
- Passwords are hashed using BCrypt
- CORS is configured for frontend origin
- Users can only access their own documents
- AWS credentials should never be committed to version control

## 🚀 Deployment

### Production Considerations

1. **Use Strong JWT Secret**: Generate a secure 256-bit key
2. **Enable HTTPS**: Use SSL/TLS certificates
3. **Database Connection Pooling**: Configure HikariCP
4. **AWS IAM Roles**: Use roles instead of access keys on EC2/ECS
5. **Environment Variables**: Use secure secret management (AWS Secrets Manager)
6. **Monitoring**: Set up CloudWatch for logs and metrics
7. **Load Balancer**: Use Application Load Balancer for scaling
8. **Route 53**: Configure DNS for custom domain

### Docker Deployment (Optional)

Create `Dockerfile` for backend:
```dockerfile
FROM openjdk:17-jdk-slim
WORKDIR /app
COPY target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

Create `Dockerfile` for frontend:
```dockerfile
FROM node:18-alpine
WORKDIR /app
COPY package*.json ./
RUN npm install
COPY . .
RUN npm run build
RUN npm install -g serve
EXPOSE 3000
CMD ["serve", "-s", "build", "-l", "3000"]
```

## 💰 Cost Estimation

For 20 files (5 MB each) per month:

- **S3**: ~$0.002
- **DynamoDB**: ~$0.0003
- **RDS PostgreSQL** (db.t3.micro): ~$12.41
- **Bedrock**: ~$0.001
- **Total**: ~$12.41/month

See AWS Pricing Calculator for detailed estimates.

## 🐛 Troubleshooting

### Backend won't start
- Check PostgreSQL is running and accessible
- Verify AWS credentials are configured
- Check application.yml configuration
- Ensure pgvector extension is installed

### Frontend can't connect to backend
- Verify backend is running on port 8080
- Check CORS configuration in SecurityConfig
- Verify REACT_APP_API_URL in .env

### OCR not working
- Install Tesseract OCR
- Set tessdata path if needed in DocumentProcessingService

### Vector search returns no results
- Ensure documents have been processed
- Check embeddings are stored in database
- Verify AWS Bedrock access

## 📝 License

This project is provided as-is for educational and commercial use.

## 👥 Contributing

Contributions are welcome! Please follow these guidelines:
1. Functions/classes must have single responsibility
2. Write unit tests as contracts of business behavior
3. Never adjust test expectations just to match code
4. Avoid magic numbers in tests
5. Make functions small, descriptive, and reusable

## 📧 Support

For issues and questions, please open an issue on GitHub.

---

Built with ❤️ using Spring Boot, React, and AWS Services

