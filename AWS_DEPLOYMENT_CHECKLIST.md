# AWS Deployment Checklist

## Pre-Deployment Checklist

### ✅ AWS Account Setup
- [ ] Create AWS account
- [ ] Set up billing alerts
- [ ] Enable MFA for root account
- [ ] Create IAM admin user (don't use root)
- [ ] Configure AWS CLI: `aws configure`

### ✅ Required AWS Services Access
- [ ] Verify Bedrock access (request model access in console)
- [ ] Check service quotas/limits
- [ ] Request limit increases if needed (e.g., VPCs, ECS tasks)

### ✅ Domain & SSL (Optional)
- [ ] Register domain name (Route 53 or external)
- [ ] Request SSL certificate in ACM
- [ ] Validate certificate

---

## Infrastructure Setup

### ✅ Networking (VPC)
- [ ] Create VPC (10.0.0.0/16)
- [ ] Create 2 public subnets (different AZs)
- [ ] Create 2 private subnets (different AZs)
- [ ] Create Internet Gateway
- [ ] Create 2 NAT Gateways (one per AZ)
- [ ] Configure route tables
- [ ] Create security groups:
  - [ ] ALB security group (allow 80, 443)
  - [ ] ECS security group (allow 8080 from ALB)
  - [ ] RDS security group (allow 5432 from ECS)

### ✅ Database (RDS)
- [ ] Create DB subnet group
- [ ] Create parameter group for pgvector
- [ ] Create RDS PostgreSQL instance
- [ ] Enable Multi-AZ (production)
- [ ] Enable encryption at rest
- [ ] Configure automated backups
- [ ] Install pgvector extension: `CREATE EXTENSION vector;`
- [ ] Run database migrations
- [ ] Test database connection

### ✅ Storage
- [ ] Create S3 bucket for documents
- [ ] Enable versioning on S3 bucket
- [ ] Enable encryption on S3 bucket
- [ ] Configure S3 lifecycle policies
- [ ] Create S3 bucket for frontend (optional)
- [ ] Create DynamoDB table (DocumentMetadata)
- [ ] Configure DynamoDB auto-scaling (production)

### ✅ Container Registry
- [ ] Create ECR repository
- [ ] Build Docker image
- [ ] Push image to ECR
- [ ] Tag image with version

---

## Application Deployment

### ✅ Backend (Spring Boot)
- [ ] Create ECS cluster
- [ ] Create ECS task definition
- [ ] Configure task role (IAM permissions)
- [ ] Set environment variables:
  - [ ] Database URL
  - [ ] S3 bucket name
  - [ ] DynamoDB table name
  - [ ] Bedrock model IDs
  - [ ] JWT secret (from Secrets Manager)
- [ ] Create ECS service
- [ ] Configure auto-scaling
- [ ] Test API endpoints

### ✅ Frontend (React)
- [ ] Build React app: `npm run build`
- [ ] Upload to S3: `aws s3 sync build/ s3://bucket-name`
- [ ] Configure S3 static website hosting (if not using CloudFront)
- [ ] Test frontend access

### ✅ Load Balancing & CDN
- [ ] Create Application Load Balancer
- [ ] Configure target group (ECS tasks)
- [ ] Configure health checks
- [ ] Attach SSL certificate (ACM)
- [ ] Configure HTTP to HTTPS redirect
- [ ] Create CloudFront distribution (optional)
- [ ] Configure CloudFront behaviors
- [ ] Test load balancer

### ✅ DNS
- [ ] Create Route 53 hosted zone
- [ ] Create A record (alias to CloudFront/ALB)
- [ ] Create CNAME for API subdomain
- [ ] Test DNS resolution

---

## Security Configuration

### ✅ IAM Roles & Policies
- [ ] Create ECS task execution role
- [ ] Create ECS task role (S3, DynamoDB, Bedrock access)
- [ ] Attach policies to roles
- [ ] Test IAM permissions

### ✅ Secrets Management
- [ ] Store database credentials in Secrets Manager
- [ ] Store JWT secret in Secrets Manager
- [ ] Update application to use Secrets Manager
- [ ] Enable secret rotation (optional)

### ✅ Network Security
- [ ] Configure security group rules
- [ ] Restrict RDS to private subnet only
- [ ] Enable VPC Flow Logs
- [ ] Configure WAF rules (optional)

### ✅ Encryption
- [ ] Enable RDS encryption at rest
- [ ] Enable S3 encryption
- [ ] Enable DynamoDB encryption
- [ ] Enable SSL/TLS for ALB
- [ ] Verify HTTPS works

---

## Monitoring & Logging

### ✅ CloudWatch Setup
- [ ] Create CloudWatch log group for ECS
- [ ] Configure ECS task logging
- [ ] Create CloudWatch dashboards:
  - [ ] ECS metrics
  - [ ] RDS metrics
  - [ ] ALB metrics
  - [ ] Application metrics
- [ ] Set up CloudWatch alarms:
  - [ ] High CPU (>80%)
  - [ ] High memory (>85%)
  - [ ] High error rate (>5%)
  - [ ] RDS storage low (<20%)
- [ ] Configure SNS notifications

### ✅ Application Monitoring
- [ ] Enable Spring Boot Actuator (if not already)
- [ ] Configure health check endpoint
- [ ] Set up application logging
- [ ] Test log aggregation

---

## Testing & Validation

### ✅ Functional Testing
- [ ] Test user registration
- [ ] Test user login
- [ ] Test document upload
- [ ] Test document download
- [ ] Test semantic search
- [ ] Test RAG Q&A
- [ ] Test document deletion

### ✅ Performance Testing
- [ ] Load test API endpoints
- [ ] Verify auto-scaling works
- [ ] Test database connection pooling
- [ ] Monitor response times

### ✅ Security Testing
- [ ] Test authentication/authorization
- [ ] Verify HTTPS only
- [ ] Test CORS configuration
- [ ] Verify secrets are not exposed

---

## Post-Deployment

### ✅ Documentation
- [ ] Document deployment process
- [ ] Document rollback procedure
- [ ] Document monitoring dashboards
- [ ] Document incident response

### ✅ Backup & Recovery
- [ ] Test RDS backup restoration
- [ ] Document backup schedule
- [ ] Test disaster recovery procedure

### ✅ Cost Optimization
- [ ] Set up cost budgets
- [ ] Review and optimize instance sizes
- [ ] Enable auto-scaling
- [ ] Configure S3 lifecycle policies
- [ ] Review CloudWatch costs

---

## Quick Commands Reference

### Database Setup
```bash
# Connect to RDS
psql -h <rds-endpoint> -U postgres -d document_management

# Install pgvector
CREATE EXTENSION vector;

# Run migrations (if using Flyway)
# Migrations should run automatically on app startup
```

### Docker Build & Push
```bash
# Build
docker build -t document-service-backend .

# Login to ECR
aws ecr get-login-password --region <region> | docker login --username AWS --password-stdin <account-id>.dkr.ecr.<region>.amazonaws.com

# Tag
docker tag document-service-backend:latest <account-id>.dkr.ecr.<region>.amazonaws.com/document-service-backend:latest

# Push
docker push <account-id>.dkr.ecr.<region>.amazonaws.com/document-service-backend:latest
```

### Frontend Deployment
```bash
cd frontend
npm install
npm run build
aws s3 sync build/ s3://document-service-frontend-prod --delete
```

### Check ECS Service Status
```bash
aws ecs describe-services --cluster <cluster-name> --services <service-name>
```

### View Logs
```bash
aws logs tail /ecs/document-service --follow
```

---

## Emergency Contacts & Resources

- **AWS Support**: https://console.aws.amazon.com/support/
- **AWS Documentation**: https://docs.aws.amazon.com/
- **AWS Status**: https://status.aws.amazon.com/
- **Cost Calculator**: https://calculator.aws/

---

**Last Updated**: 2024
