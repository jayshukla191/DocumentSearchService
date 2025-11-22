# Quick Setup Guide

## Prerequisites Checklist

- [ ] Java 17+ installed
- [ ] Node.js 18+ and npm installed
- [ ] PostgreSQL 14+ installed
- [ ] pgvector extension installed
- [ ] Maven 3.6+ installed
- [ ] AWS account with Bedrock access
- [ ] AWS CLI configured

## Step-by-Step Setup

### 1. Database Setup (5 minutes)

```bash
# Create database
psql -U postgres
```

```sql
CREATE DATABASE document_management;
\c document_management
CREATE EXTENSION vector;
\q
```

### 2. AWS Setup (10 minutes)

1. Create S3 bucket:
```bash
aws s3 mb s3://your-document-bucket-name
```

2. Create DynamoDB table:
```bash
aws dynamodb create-table \
    --table-name DocumentMetadata \
    --attribute-definitions AttributeName=DocumentId,AttributeType=S \
    --key-schema AttributeName=DocumentId,KeyType=HASH \
    --billing-mode PAY_PER_REQUEST
```

3. Verify Bedrock access:
```bash
aws bedrock list-foundation-models --region us-east-1
```

### 3. Backend Configuration (2 minutes)

Edit `backend/src/main/resources/application.yml`:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/document_management
    username: YOUR_POSTGRES_USERNAME
    password: YOUR_POSTGRES_PASSWORD

aws:
  region: us-east-1
  s3:
    bucket-name: your-document-bucket-name
  dynamodb:
    table-name: DocumentMetadata

jwt:
  secret: CHANGE_THIS_TO_A_SECURE_256_BIT_KEY
```

### 4. Start Backend (2 minutes)

```bash
cd backend
mvn clean install
mvn spring-boot:run
```

Wait for: "Started DocumentServiceApplication"

### 5. Start Frontend (2 minutes)

```bash
cd frontend
npm install
npm start
```

Browser opens at: http://localhost:3000

## Verification

### Test Backend
```bash
curl http://localhost:8080/api/auth/register -X POST \
  -H "Content-Type: application/json" \
  -d '{"username":"test","email":"test@example.com","password":"test123"}'
```

### Test Frontend
1. Open http://localhost:3000
2. Click "Register"
3. Create account and login
4. Upload a test document

## Common Issues

### Backend: "Unable to connect to PostgreSQL"
- Verify PostgreSQL is running: `sudo service postgresql status`
- Check database exists: `psql -U postgres -l`
- Verify credentials in application.yml

### Backend: "AWS credentials not found"
- Run: `aws configure`
- Or set environment variables: `AWS_ACCESS_KEY_ID`, `AWS_SECRET_ACCESS_KEY`

### Frontend: "Network Error"
- Verify backend is running on port 8080
- Check CORS settings in SecurityConfig.java
- Verify .env file has correct API URL

### pgvector: "Extension does not exist"
- Install pgvector: https://github.com/pgvector/pgvector
- Run: `CREATE EXTENSION vector;` in your database

## Next Steps

1. Upload your first document (PDF, image, or text)
2. Wait a few seconds for processing
3. Try semantic search: "What is this about?"
4. Ask questions in the Q&A page

## Production Deployment

See README.md for production deployment guidelines including:
- Docker containerization
- Load balancer setup
- Route 53 DNS configuration
- Security best practices
- Monitoring and logging

## Need Help?

- Check README.md for detailed documentation
- Review troubleshooting section
- Check application logs in console
- Verify AWS service quotas and limits

