# AWS Deployment Guide for Document Management Service

## 📋 Overview

This guide provides comprehensive recommendations for deploying your Document Management Service on AWS. The application consists of:
- **Backend**: Spring Boot REST API (Java 17)
- **Frontend**: React SPA
- **Database**: PostgreSQL with pgvector extension
- **AI/ML**: AWS Bedrock (Titan models)
- **Storage**: AWS S3 and DynamoDB

---

## 🏗️ Recommended AWS Architecture

### Option 1: Containerized Deployment (Recommended for Production)

```
┌─────────────────────────────────────────────────────────────┐
│                        CloudFront                            │
│                    (CDN + SSL/TLS)                          │
└───────────────────────┬─────────────────────────────────────┘
                        │
        ┌───────────────┴───────────────┐
        │                               │
┌───────▼────────┐            ┌─────────▼──────────┐
│  Application   │            │   Static Frontend  │
│  Load Balancer │            │   (S3 + CloudFront)│
└───────┬────────┘            └────────────────────┘
        │
        ├─────────────────┬─────────────────┐
        │                 │                 │
┌───────▼────────┐ ┌──────▼──────┐ ┌───────▼────────┐
│   ECS Fargate  │ │  ECS Fargate │ │  ECS Fargate  │
│   (Backend)    │ │  (Backend)   │ │  (Backend)    │
└───────┬────────┘ └──────┬──────┘ └───────┬────────┘
        │                 │                 │
        └─────────────────┴─────────────────┘
                        │
        ┌───────────────┴───────────────┐
        │                               │
┌───────▼────────┐            ┌─────────▼──────────┐
│  RDS PostgreSQL│            │   ElastiCache      │
│  (Multi-AZ)    │            │   (Redis - Optional)│
└────────────────┘            └────────────────────┘
        │
        │
┌───────▼────────┐  ┌─────────▼──────────┐  ┌───────▼────────┐
│      S3        │  │    DynamoDB        │  │    Bedrock     │
│  (Documents)   │  │  (Metadata)        │  │  (AI/ML)       │
└────────────────┘  └────────────────────┘  └────────────────┘
```

### Option 2: Serverless Deployment (Cost-Effective for Low-Medium Traffic)

```
┌─────────────────────────────────────────────────────────────┐
│                        CloudFront                            │
│                    (CDN + SSL/TLS)                          │
└───────────────────────┬─────────────────────────────────────┘
                        │
        ┌───────────────┴───────────────┐
        │                               │
┌───────▼────────┐            ┌─────────▼──────────┐
│   API Gateway  │            │   Static Frontend  │
│                │            │   (S3 + CloudFront)│
└───────┬────────┘            └────────────────────┘
        │
        │
┌───────▼────────┐
│  Lambda +      │
│  Lambda Layers │
│  (Backend API) │
└───────┬────────┘
        │
┌───────▼────────┐  ┌─────────▼──────────┐  ┌───────▼────────┐
│  RDS PostgreSQL│  │    DynamoDB        │  │    Bedrock     │
│  (Serverless)  │  │  (Metadata)        │  │  (AI/ML)       │
└────────────────┘  └────────────────────┘  └────────────────┘
```

---

## 🔧 Required AWS Services

### 1. **Compute Services**

#### Option A: Amazon ECS with Fargate (Recommended)
- **Service**: Amazon ECS (Elastic Container Service)
- **Launch Type**: Fargate (serverless containers)
- **Why**: 
  - No EC2 management overhead
  - Auto-scaling capabilities
  - Pay only for running containers
  - Integrated with ALB for load balancing
- **Configuration**:
  - Task CPU: 1 vCPU (can scale to 4 vCPU)
  - Task Memory: 2 GB (can scale to 8 GB)
  - Desired Count: 2-3 tasks for high availability
  - Auto-scaling: Scale based on CPU/Memory utilization

#### Option B: AWS Elastic Beanstalk
- **Service**: AWS Elastic Beanstalk
- **Why**: 
  - Easy deployment for Spring Boot applications
  - Automatic load balancing and auto-scaling
  - Built-in monitoring
- **Configuration**:
  - Platform: Java 17 (Corretto)
  - Instance Type: t3.medium (2 vCPU, 4 GB RAM)
  - Load Balancer: Application Load Balancer
  - Auto-scaling: Min 2, Max 10 instances

#### Option C: Amazon EC2 (Traditional)
- **Service**: Amazon EC2
- **Why**: Full control, cost-effective for predictable workloads
- **Configuration**:
  - Instance Type: t3.large (2 vCPU, 8 GB RAM)
  - AMI: Amazon Linux 2023
  - Auto Scaling Group: Min 2, Max 10 instances
  - Load Balancer: Application Load Balancer

### 2. **Database Services**

#### Amazon RDS for PostgreSQL
- **Service**: Amazon RDS PostgreSQL
- **Version**: PostgreSQL 14+ (supports pgvector)
- **Configuration**:
  - **Instance Class**: 
    - Development: db.t3.micro (1 vCPU, 1 GB RAM)
    - Production: db.r6g.large (2 vCPU, 16 GB RAM) or db.r6g.xlarge (4 vCPU, 32 GB RAM)
  - **Storage**: 
    - Type: gp3 (General Purpose SSD)
    - Size: 100 GB (auto-scaling enabled)
    - IOPS: 3000 (provisioned)
  - **Multi-AZ**: Enabled for production (high availability)
  - **Backup**: 
    - Automated backups: 7 days retention
    - Point-in-time recovery: Enabled
  - **Security**:
    - Encryption at rest: Enabled
    - Encryption in transit: Enabled (SSL required)
    - VPC: Private subnet only
  - **pgvector Extension**: Install via parameter group

#### Amazon ElastiCache (Optional - for caching)
- **Service**: Amazon ElastiCache for Redis
- **Why**: Cache frequently accessed documents and search results
- **Configuration**:
  - Node Type: cache.t3.micro (development) or cache.t3.small (production)
  - Engine: Redis 7.x
  - Multi-AZ: Enabled for production

### 3. **Storage Services**

#### Amazon S3
- **Service**: Amazon S3
- **Buckets Needed**:
  1. **Document Storage Bucket**
     - Name: `document-service-files-prod`
     - Versioning: Enabled
     - Encryption: AES-256 (SSE-S3) or KMS
     - Lifecycle Policies:
       - Move to Glacier after 90 days
       - Delete incomplete multipart uploads after 7 days
     - CORS: Configured for frontend access
     - Access: Private (only via IAM roles)
  
  2. **Frontend Static Hosting Bucket** (Optional)
     - Name: `document-service-frontend-prod`
     - Static website hosting: Enabled
     - CloudFront distribution: Connected

#### Amazon DynamoDB
- **Service**: Amazon DynamoDB
- **Table**: DocumentMetadata
- **Configuration**:
  - Partition Key: DocumentId (String)
  - Billing Mode: 
    - Development: On-Demand
    - Production: Provisioned (1000 RCU, 1000 WCU) with auto-scaling
  - Encryption: Enabled (AWS managed keys)
  - Point-in-time Recovery: Enabled
  - Global Tables: Optional (for multi-region)

### 4. **Networking Services**

#### Amazon VPC (Virtual Private Cloud)
- **Service**: Amazon VPC
- **Configuration**:
  - **CIDR**: 10.0.0.0/16
  - **Subnets**:
    - Public Subnets: 10.0.1.0/24, 10.0.2.0/24 (for ALB, NAT Gateway)
    - Private Subnets: 10.0.10.0/24, 10.0.11.0/24 (for ECS tasks, RDS)
  - **Internet Gateway**: For public subnets
  - **NAT Gateway**: For private subnets (2 for high availability)
  - **Route Tables**: Separate for public and private subnets
  - **Security Groups**:
    - ALB Security Group: Allow HTTP (80), HTTPS (443) from internet
    - ECS Security Group: Allow traffic from ALB only
    - RDS Security Group: Allow PostgreSQL (5432) from ECS security group only

#### Application Load Balancer (ALB)
- **Service**: Application Load Balancer
- **Configuration**:
  - Type: Application Load Balancer
  - Scheme: Internet-facing
  - Subnets: Public subnets (multi-AZ)
  - Listeners:
    - HTTP (80): Redirect to HTTPS
    - HTTPS (443): SSL certificate from ACM
  - Target Group:
    - Protocol: HTTP
    - Port: 8080
    - Health Check: `/actuator/health` (if Spring Boot Actuator enabled)
  - SSL Certificate: AWS Certificate Manager (ACM)

#### Amazon CloudFront
- **Service**: Amazon CloudFront
- **Why**: 
  - CDN for frontend static assets
  - SSL/TLS termination
  - DDoS protection
  - Geographic distribution
- **Configuration**:
  - Origin: S3 bucket (frontend) or ALB (API)
  - Behaviors:
    - Frontend: Cache static assets (CSS, JS, images)
    - API: No cache, forward all headers
  - SSL Certificate: ACM
  - Price Class: Use all edge locations (or optimize for cost)

#### Route 53
- **Service**: Amazon Route 53
- **Why**: DNS management and domain routing
- **Configuration**:
  - Hosted Zone: Create for your domain
  - Record Types:
    - A Record: Alias to CloudFront distribution
    - CNAME: API subdomain to ALB

### 5. **AI/ML Services**

#### Amazon Bedrock
- **Service**: Amazon Bedrock
- **Models Used**:
  - Embeddings: `amazon.titan-embed-text-v2:0`
  - LLM: `amazon.titan-text-lite-v1`
- **Configuration**:
  - Region: Ensure Bedrock is available in your region
  - Access: IAM role with Bedrock permissions
  - Model Access: Request access in Bedrock console
- **Cost Optimization**:
  - Use caching for embeddings (ElastiCache)
  - Batch processing for document uploads

### 6. **Security Services**

#### AWS IAM (Identity and Access Management)
- **Service**: AWS IAM
- **Roles Needed**:
  1. **ECS Task Role**:
     - Permissions: S3 (read/write), DynamoDB (read/write), Bedrock (invoke)
     - Trust: ECS tasks
  2. **EC2 Instance Role** (if using EC2):
     - Same permissions as ECS Task Role
  3. **RDS Access**: Via security groups (no IAM role needed)

#### AWS Secrets Manager
- **Service**: AWS Secrets Manager
- **Secrets to Store**:
  - Database credentials (RDS)
  - JWT secret key
  - AWS access keys (if not using IAM roles)
- **Rotation**: Enable automatic rotation for RDS credentials

#### AWS WAF (Web Application Firewall)
- **Service**: AWS WAF
- **Why**: Protect against common web exploits
- **Configuration**:
  - Attach to CloudFront or ALB
  - Rules:
    - AWS Managed Rules: Core rule set
    - Rate limiting: 2000 requests per 5 minutes per IP
    - Geo-blocking: Optional (block specific countries)

#### AWS Certificate Manager (ACM)
- **Service**: AWS Certificate Manager
- **Why**: Free SSL/TLS certificates
- **Configuration**:
  - Certificate: Request public certificate
  - Domain: Your domain name (e.g., `api.yourdomain.com`)
  - Validation: DNS or email validation
  - Use: Attach to ALB and CloudFront

### 7. **Monitoring & Logging Services**

#### Amazon CloudWatch
- **Service**: Amazon CloudWatch
- **Metrics to Monitor**:
  - ECS: CPU utilization, memory utilization, task count
  - RDS: CPU utilization, database connections, storage space
  - ALB: Request count, response time, error rates
  - S3: Bucket size, number of objects
  - DynamoDB: Read/Write capacity, throttling
- **Logs**:
  - ECS Task Logs: Send to CloudWatch Logs
  - Application Logs: Spring Boot logs to CloudWatch
- **Alarms**:
  - High CPU (>80%)
  - High memory (>85%)
  - Database connections (>80% of max)
  - Error rate (>5%)
  - RDS storage (<20% free)

#### AWS X-Ray (Optional)
- **Service**: AWS X-Ray
- **Why**: Distributed tracing for debugging
- **Configuration**:
  - Enable in Spring Boot application
  - Trace API requests end-to-end

### 8. **CI/CD Services**

#### AWS CodePipeline
- **Service**: AWS CodePipeline
- **Pipeline Stages**:
  1. Source: GitHub/GitLab/CodeCommit
  2. Build: CodeBuild
  3. Deploy: ECS/Elastic Beanstalk

#### AWS CodeBuild
- **Service**: AWS CodeBuild
- **Build Spec**:
  - Backend: Maven build, create Docker image, push to ECR
  - Frontend: npm build, upload to S3
- **Configuration**:
  - Environment: Java 17 (Corretto)
  - Compute: 3 GB memory, 2 vCPU

#### Amazon ECR (Elastic Container Registry)
- **Service**: Amazon ECR
- **Why**: Store Docker images
- **Configuration**:
  - Repository: `document-service-backend`
  - Image Scanning: Enabled
  - Lifecycle Policy: Keep last 10 images

### 9. **Additional Services**

#### AWS Systems Manager Parameter Store
- **Service**: AWS Systems Manager Parameter Store
- **Why**: Store configuration parameters
- **Parameters**:
  - Application configuration (non-sensitive)
  - Environment variables

#### AWS Backup (Optional)
- **Service**: AWS Backup
- **Why**: Centralized backup management
- **Backup Plans**:
  - RDS: Daily backups, 30-day retention
  - EBS volumes: Weekly snapshots

---

## 📊 Cost Estimation (Monthly)

### Small Scale (Development/Testing)
- **ECS Fargate**: 2 tasks × 0.5 vCPU × 1 GB × 730 hours = ~$30
- **RDS PostgreSQL**: db.t3.micro = ~$15
- **S3**: 100 GB storage = ~$2.30
- **DynamoDB**: On-demand = ~$1-5
- **ALB**: ~$16
- **NAT Gateway**: 2 × ~$32 = ~$64
- **CloudFront**: ~$1-5
- **Bedrock**: ~$1-10 (based on usage)
- **Total**: ~$130-150/month

### Medium Scale (Production)
- **ECS Fargate**: 3 tasks × 1 vCPU × 2 GB × 730 hours = ~$90
- **RDS PostgreSQL**: db.r6g.large (Multi-AZ) = ~$300
- **S3**: 500 GB storage = ~$11.50
- **DynamoDB**: Provisioned (1000 RCU/WCU) = ~$50
- **ALB**: ~$16
- **NAT Gateway**: 2 × ~$32 = ~$64
- **CloudFront**: ~$10-20
- **Bedrock**: ~$50-200 (based on usage)
- **ElastiCache**: cache.t3.small = ~$15
- **Total**: ~$600-800/month

### Large Scale (High Traffic)
- **ECS Fargate**: 5-10 tasks × 2 vCPU × 4 GB = ~$300-600
- **RDS PostgreSQL**: db.r6g.xlarge (Multi-AZ) = ~$1,200
- **S3**: 2 TB storage = ~$46
- **DynamoDB**: Provisioned (5000 RCU/WCU) = ~$250
- **ALB**: ~$16
- **NAT Gateway**: 2 × ~$32 = ~$64
- **CloudFront**: ~$50-100
- **Bedrock**: ~$200-500
- **ElastiCache**: cache.t3.medium = ~$30
- **Total**: ~$2,200-2,800/month

**Note**: Costs vary by region and usage. Use AWS Pricing Calculator for accurate estimates.

---

## 🚀 Deployment Steps

### Phase 1: Infrastructure Setup

1. **Create VPC and Networking**
   ```bash
   # Use AWS Console or CloudFormation/Terraform
   # Create VPC with public and private subnets
   # Configure Internet Gateway and NAT Gateways
   ```

2. **Set up RDS PostgreSQL**
   ```bash
   aws rds create-db-instance \
     --db-instance-identifier document-db \
     --db-instance-class db.r6g.large \
     --engine postgres \
     --engine-version 14.10 \
     --master-username postgres \
     --master-user-password <secure-password> \
     --allocated-storage 100 \
     --storage-type gp3 \
     --vpc-security-group-ids <security-group-id> \
     --db-subnet-group-name <subnet-group> \
     --backup-retention-period 7 \
     --multi-az \
     --storage-encrypted
   ```

3. **Install pgvector Extension**
   ```sql
   -- Connect to RDS instance
   CREATE EXTENSION IF NOT EXISTS vector;
   ```

4. **Create S3 Buckets**
   ```bash
   aws s3 mb s3://document-service-files-prod
   aws s3 mb s3://document-service-frontend-prod
   ```

5. **Create DynamoDB Table**
   ```bash
   aws dynamodb create-table \
     --table-name DocumentMetadata \
     --attribute-definitions AttributeName=DocumentId,AttributeType=S \
     --key-schema AttributeName=DocumentId,KeyType=HASH \
     --billing-mode PROVISIONED \
     --provisioned-throughput ReadCapacityUnits=1000,WriteCapacityUnits=1000
   ```

### Phase 2: Application Deployment

1. **Create ECR Repository**
   ```bash
   aws ecr create-repository --repository-name document-service-backend
   ```

2. **Build and Push Docker Image**
   ```bash
   # Build Docker image
   docker build -t document-service-backend .
   
   # Tag and push to ECR
   aws ecr get-login-password --region us-east-1 | docker login --username AWS --password-stdin <account-id>.dkr.ecr.us-east-1.amazonaws.com
   docker tag document-service-backend:latest <account-id>.dkr.ecr.us-east-1.amazonaws.com/document-service-backend:latest
   docker push <account-id>.dkr.ecr.us-east-1.amazonaws.com/document-service-backend:latest
   ```

3. **Create ECS Cluster and Service**
   ```bash
   # Create cluster
   aws ecs create-cluster --cluster-name document-service-cluster
   
   # Create task definition (via console or JSON)
   # Create service with desired count 2-3
   ```

4. **Deploy Frontend to S3**
   ```bash
   cd frontend
   npm run build
   aws s3 sync build/ s3://document-service-frontend-prod --delete
   ```

5. **Set up CloudFront Distribution**
   - Create distribution pointing to S3 bucket
   - Configure SSL certificate from ACM
   - Set up behaviors for caching

### Phase 3: Configuration

1. **Store Secrets in Secrets Manager**
   ```bash
   aws secretsmanager create-secret \
     --name document-service/db-credentials \
     --secret-string '{"username":"postgres","password":"<password>"}'
   ```

2. **Update application.yml**
   - Use environment variables for sensitive data
   - Reference Secrets Manager for credentials
   - Update RDS endpoint
   - Configure S3 bucket names

3. **Set up IAM Roles**
   - Create ECS task role with S3, DynamoDB, Bedrock permissions
   - Attach to ECS task definition

### Phase 4: Monitoring and Optimization

1. **Set up CloudWatch Alarms**
2. **Configure Log Aggregation**
3. **Set up Auto-scaling Policies**
4. **Enable Cost Monitoring**

---

## 🔐 Security Best Practices

1. **Network Security**
   - Use private subnets for databases and application servers
   - Restrict security groups to minimum required ports
   - Use VPC endpoints for AWS services (reduce NAT Gateway costs)

2. **Data Security**
   - Enable encryption at rest for RDS, S3, DynamoDB
   - Enable encryption in transit (SSL/TLS)
   - Use AWS KMS for key management
   - Rotate credentials regularly

3. **Application Security**
   - Use IAM roles instead of access keys
   - Store secrets in Secrets Manager
   - Enable WAF rules
   - Implement rate limiting
   - Use strong JWT secrets (256+ bits)

4. **Compliance**
   - Enable CloudTrail for audit logging
   - Enable VPC Flow Logs
   - Regular security audits

---

## 📝 Environment-Specific Configurations

### Development Environment
- Smaller instance sizes
- Single-AZ RDS
- On-demand DynamoDB
- Minimal monitoring
- No WAF (or basic rules)

### Staging Environment
- Medium instance sizes
- Multi-AZ RDS (optional)
- Provisioned DynamoDB
- Full monitoring
- WAF enabled

### Production Environment
- Larger instance sizes
- Multi-AZ RDS (required)
- Provisioned DynamoDB with auto-scaling
- Comprehensive monitoring and alerting
- Full WAF protection
- Backup and disaster recovery

---

## 🛠️ Infrastructure as Code (IaC)

### Recommended Tools:
1. **AWS CloudFormation**: Native AWS IaC
2. **Terraform**: Multi-cloud IaC (recommended)
3. **AWS CDK**: Infrastructure as code using familiar languages

### Sample Terraform Structure:
```
terraform/
├── main.tf
├── variables.tf
├── outputs.tf
├── modules/
│   ├── vpc/
│   ├── rds/
│   ├── ecs/
│   ├── s3/
│   └── dynamodb/
└── environments/
    ├── dev/
    ├── staging/
    └── prod/
```

---

## 🎯 Next Steps

1. **Choose Deployment Option**: ECS Fargate (recommended) or Elastic Beanstalk
2. **Set up AWS Account**: Create account, set up billing alerts
3. **Create VPC**: Set up networking infrastructure
4. **Deploy Database**: Create RDS instance with pgvector
5. **Build Docker Image**: Containerize Spring Boot application
6. **Set up ECS**: Create cluster and deploy service
7. **Deploy Frontend**: Upload to S3 and configure CloudFront
8. **Configure Monitoring**: Set up CloudWatch alarms
9. **Test**: Verify all services are working
10. **Optimize**: Review costs and performance

---

## 📚 Additional Resources

- [AWS Well-Architected Framework](https://aws.amazon.com/architecture/well-architected/)
- [AWS Pricing Calculator](https://calculator.aws/)
- [ECS Best Practices](https://docs.aws.amazon.com/AmazonECS/latest/bestpracticesguide/intro.html)
- [RDS PostgreSQL Best Practices](https://docs.aws.amazon.com/AmazonRDS/latest/UserGuide/CHAP_BestPractices.html)
- [pgvector on RDS](https://github.com/pgvector/pgvector)

---

## 💡 Cost Optimization Tips

1. **Use Reserved Instances**: For predictable workloads (RDS, EC2)
2. **Right-size Instances**: Monitor and adjust instance sizes
3. **Use Spot Instances**: For non-critical workloads (development)
4. **S3 Lifecycle Policies**: Move old files to Glacier
5. **CloudFront Caching**: Reduce origin requests
6. **VPC Endpoints**: Reduce NAT Gateway data transfer costs
7. **Auto-scaling**: Scale down during off-peak hours
8. **Bedrock Caching**: Cache embeddings to reduce API calls

---

**Last Updated**: 2024
**Version**: 1.0
