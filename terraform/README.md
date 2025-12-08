# Document Service Infrastructure - Terraform

This directory contains Terraform configurations for deploying the Document Service infrastructure on AWS.

## Architecture Overview

```
┌─────────────────────────────────────────────────────────────────────────────────┐
│                                    AWS Cloud                                     │
│                                                                                  │
│  ┌─────────────┐     ┌─────────────┐     ┌─────────────────────────────────┐   │
│  │   Route 53  │────▶│ CloudFront  │────▶│        S3 (Frontend)            │   │
│  └─────────────┘     └──────┬──────┘     └─────────────────────────────────┘   │
│                             │                                                    │
│                             ▼                                                    │
│  ┌──────────────────────────────────────────────────────────────────────────┐  │
│  │                              VPC (10.0.0.0/16)                            │  │
│  │  ┌────────────────────────────────────────────────────────────────────┐  │  │
│  │  │                    Public Subnets (10.0.1.0/24, 10.0.2.0/24)       │  │  │
│  │  │  ┌─────────────┐     ┌─────────────┐     ┌─────────────┐          │  │  │
│  │  │  │    NAT GW   │     │    NAT GW   │     │     ALB     │◀─────────│  │  │
│  │  │  └──────┬──────┘     └──────┬──────┘     └──────┬──────┘          │  │  │
│  │  └─────────┼───────────────────┼───────────────────┼──────────────────┘  │  │
│  │            │                   │                   │                      │  │
│  │  ┌─────────┼───────────────────┼───────────────────┼──────────────────┐  │  │
│  │  │         ▼                   ▼                   ▼                  │  │  │
│  │  │                  Private Subnets (10.0.10.0/24, 10.0.11.0/24)      │  │  │
│  │  │  ┌─────────────────────────────────────────────────────────────┐  │  │  │
│  │  │  │                    ECS Fargate Cluster                      │  │  │  │
│  │  │  │  ┌─────────┐  ┌─────────┐  ┌─────────┐                     │  │  │  │
│  │  │  │  │  Task   │  │  Task   │  │  Task   │                     │  │  │  │
│  │  │  │  │ (8080)  │  │ (8080)  │  │ (8080)  │                     │  │  │  │
│  │  │  │  └────┬────┘  └────┬────┘  └────┬────┘                     │  │  │  │
│  │  │  └───────┼────────────┼────────────┼───────────────────────────┘  │  │  │
│  │  │          │            │            │                               │  │  │
│  │  │          ▼            ▼            ▼                               │  │  │
│  │  │  ┌─────────────────────────────────────────┐                      │  │  │
│  │  │  │         RDS PostgreSQL (pgvector)       │                      │  │  │
│  │  │  │              Multi-AZ                   │                      │  │  │
│  │  │  └─────────────────────────────────────────┘                      │  │  │
│  │  └────────────────────────────────────────────────────────────────────┘  │  │
│  └──────────────────────────────────────────────────────────────────────────┘  │
│                                                                                  │
│  ┌────────────────┐  ┌────────────────┐  ┌────────────────┐                    │
│  │  S3 (Documents)│  │    DynamoDB    │  │ Amazon Bedrock │                    │
│  └────────────────┘  └────────────────┘  └────────────────┘                    │
└─────────────────────────────────────────────────────────────────────────────────┘
```

## Directory Structure

```
terraform/
├── main.tf                 # Main configuration (module orchestration)
├── variables.tf            # Input variables
├── outputs.tf              # Output values
├── versions.tf             # Provider versions
├── modules/
│   ├── vpc/               # VPC, subnets, NAT gateways
│   ├── security-groups/   # Security groups for ALB, ECS, RDS
│   ├── rds/               # RDS PostgreSQL with pgvector
│   ├── s3/                # S3 buckets (documents, frontend)
│   ├── dynamodb/          # DynamoDB table
│   ├── iam/               # IAM roles and policies
│   ├── alb/               # Application Load Balancer
│   ├── ecs/               # ECS Fargate cluster and services
│   ├── cloudfront/        # CloudFront distribution
│   ├── cloudwatch/        # CloudWatch dashboards and alarms
│   └── codepipeline/      # CI/CD pipeline
├── environments/
│   ├── dev/               # Development environment
│   │   ├── main.tf
│   │   ├── variables.tf
│   │   ├── outputs.tf
│   │   └── terraform.tfvars
│   └── prod/              # Production environment
│       ├── main.tf
│       ├── variables.tf
│       ├── outputs.tf
│       └── terraform.tfvars
└── README.md
```

## Prerequisites

1. **AWS Account** with appropriate permissions
2. **Terraform** >= 1.5.0 installed
3. **AWS CLI** configured with credentials
4. **Docker** (for building images)

### Required AWS Permissions

The IAM user/role needs permissions for:
- VPC, EC2 (networking)
- ECS, ECR
- RDS
- S3
- DynamoDB
- IAM (role creation)
- CloudWatch
- CloudFront
- Secrets Manager
- CodePipeline, CodeBuild (if using CI/CD)
- Amazon Bedrock

## Quick Start

### 1. Clone and Navigate

```bash
cd terraform/environments/dev
```

### 2. Configure Variables

Edit `terraform.tfvars` with your settings:

```hcl
project_name = "document-service"
environment  = "dev"
aws_region   = "us-east-1"

# Add your alarm email
alarm_email = "your-email@example.com"

# Configure GitHub repo for CI/CD (optional)
enable_codepipeline = true
github_repository   = "your-org/document-service"
```

### 3. Initialize Terraform

```bash
terraform init
```

### 4. Plan and Review

```bash
terraform plan
```

### 5. Apply Infrastructure

```bash
terraform apply
```

### 6. Get Outputs

```bash
terraform output
```

## Environment Configuration

### Development (Cost-Optimized)

| Resource | Configuration | Est. Cost/Month |
|----------|---------------|-----------------|
| ECS Fargate | 0.5 vCPU, 1GB RAM, 1 task | ~$15 |
| RDS | db.t3.micro | ~$15 |
| NAT Gateway | 1 (single AZ) | ~$32 |
| S3 | Standard | ~$2 |
| DynamoDB | On-Demand | ~$1-5 |
| ALB | 1 | ~$16 |
| **Total** | | **~$80-100** |

### Production (High Availability)

| Resource | Configuration | Est. Cost/Month |
|----------|---------------|-----------------|
| ECS Fargate | 1 vCPU, 2GB RAM, 3 tasks | ~$90 |
| RDS | db.r6g.large, Multi-AZ | ~$300 |
| NAT Gateway | 2 (multi-AZ) | ~$64 |
| S3 | Standard + Glacier | ~$12 |
| DynamoDB | Provisioned (100 RCU/WCU) | ~$50 |
| ALB | 1 | ~$16 |
| CloudFront | Standard | ~$10-20 |
| **Total** | | **~$550-700** |

## Module Details

### VPC Module
- Creates VPC with public and private subnets
- Internet Gateway for public subnets
- NAT Gateway(s) for private subnet internet access
- Route tables for each subnet type
- VPC Flow Logs (production only)

### Security Groups Module
- **ALB SG**: Allows HTTP (80) and HTTPS (443) from internet
- **ECS SG**: Allows traffic only from ALB on port 8080
- **RDS SG**: Allows PostgreSQL (5432) only from ECS

### RDS Module
- PostgreSQL 14 with pgvector extension support
- Encrypted storage (AES-256)
- Automated backups with PITR
- Secrets Manager integration for credentials
- Enhanced monitoring (production)
- Multi-AZ deployment (production)

### S3 Module
- **Documents Bucket**: Versioning, lifecycle policies, CORS
- **Frontend Bucket**: Static website hosting, CloudFront OAC
- Server-side encryption
- Glacier transition for old documents

### DynamoDB Module
- On-demand or provisioned capacity
- Point-in-time recovery (production)
- Global secondary index on UserId
- Auto-scaling for provisioned mode

### ECS Module
- Fargate launch type
- ECR repository with lifecycle policies
- Auto-scaling based on CPU/memory
- Container Insights (production)
- Blue-green deployments via CodePipeline

### CloudFront Module
- S3 origin for frontend
- ALB origin for API
- Custom error pages for SPA routing
- TLS 1.2+ enforcement
- Geographic distribution

### CloudWatch Module
- Unified dashboard
- Alarms for CPU, memory, storage, errors
- Log metric filters
- SNS notifications

## Post-Deployment Steps

### 1. Install pgvector Extension

After RDS is created, connect and run:

```sql
CREATE EXTENSION vector;
```

### 2. Build and Push Docker Image

```bash
# Login to ECR
aws ecr get-login-password --region us-east-1 | docker login --username AWS --password-stdin <account-id>.dkr.ecr.us-east-1.amazonaws.com

# Build image
cd backend
docker build -t document-service-backend .

# Tag and push
docker tag document-service-backend:latest <ecr-repo-url>:latest
docker push <ecr-repo-url>:latest
```

### 3. Deploy Frontend to S3

```bash
cd frontend
npm run build
aws s3 sync build/ s3://<frontend-bucket-name>/
```

### 4. Invalidate CloudFront Cache

```bash
aws cloudfront create-invalidation --distribution-id <dist-id> --paths "/*"
```

### 5. Enable Bedrock Models

In AWS Console, navigate to Amazon Bedrock and request access to:
- `amazon.titan-embed-text-v2:0`
- `amazon.titan-text-lite-v1`

## Remote State Configuration

For team collaboration, configure remote state:

```hcl
# In environments/dev/main.tf or environments/prod/main.tf
terraform {
  backend "s3" {
    bucket         = "your-terraform-state-bucket"
    key            = "document-service/dev/terraform.tfstate"
    region         = "us-east-1"
    encrypt        = true
    dynamodb_table = "terraform-state-lock"
  }
}
```

Create the state bucket and DynamoDB table first:

```bash
aws s3 mb s3://your-terraform-state-bucket
aws dynamodb create-table \
  --table-name terraform-state-lock \
  --attribute-definitions AttributeName=LockID,AttributeType=S \
  --key-schema AttributeName=LockID,KeyType=HASH \
  --billing-mode PAY_PER_REQUEST
```

## CI/CD Pipeline (CodePipeline)

When enabled, the pipeline:

1. **Source**: Triggered by GitHub commits
2. **Build**: Maven build, Docker image creation, ECR push
3. **Deploy**: ECS service update with new image

### Setup Steps:

1. Enable `enable_codepipeline = true`
2. Set `github_repository = "org/repo"`
3. Apply Terraform
4. In AWS Console, approve the GitHub CodeStar connection

## Security Best Practices

1. **Secrets**: All secrets stored in AWS Secrets Manager
2. **Encryption**: 
   - RDS: Encrypted at rest and in transit
   - S3: Server-side encryption enabled
   - DynamoDB: Encryption enabled
3. **Network**: 
   - RDS in private subnets only
   - ECS in private subnets with NAT Gateway
   - Security groups with minimal permissions
4. **IAM**: Least privilege policies for ECS tasks
5. **Logging**: VPC Flow Logs, CloudWatch Logs, S3 access logs

## Troubleshooting

### ECS Tasks Not Starting

```bash
# Check task logs
aws logs tail /ecs/document-service-dev-backend --follow

# Check service events
aws ecs describe-services --cluster <cluster-name> --services <service-name>
```

### RDS Connection Issues

1. Verify security group allows traffic from ECS
2. Check RDS endpoint in task environment variables
3. Verify password in Secrets Manager

### CloudFront 403 Errors

1. Check S3 bucket policy allows CloudFront OAC
2. Verify index.html exists in bucket
3. Check CloudFront distribution status

## Clean Up

To destroy all resources:

```bash
cd terraform/environments/dev  # or prod
terraform destroy
```

**Warning**: For production, ensure you have backups before destroying!

## Support

For issues or questions:
1. Check CloudWatch logs
2. Review terraform plan output
3. Verify AWS permissions
4. Check security group rules

## License

This infrastructure code is part of the Document Service project.
