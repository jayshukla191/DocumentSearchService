# Document Service - Terraform Infrastructure

This directory contains Terraform configurations to deploy the Document Service application to AWS.

## Architecture

The infrastructure includes:
- **VPC** with public and private subnets across 2 AZs
- **ECS Fargate** for running the containerized backend
- **RDS PostgreSQL** with pgvector extension for vector storage
- **S3** buckets for document storage and frontend hosting
- **DynamoDB** for document metadata
- **CloudFront** CDN for global distribution
- **ALB** for load balancing
- **Secrets Manager** for secure credential storage

## Prerequisites

1. **AWS CLI** configured with appropriate credentials
2. **Terraform** v1.5.0 or later
3. **Docker** for building container images

## Project Structure

```
terraform/
├── environments/          # Environment-specific configurations
│   ├── dev/              # Development environment
│   └── prod/             # Production environment
├── modules/              # Reusable Terraform modules
│   ├── vpc/             # VPC, subnets, NAT, security groups
│   ├── rds/             # PostgreSQL with pgvector
│   ├── s3/              # S3 buckets
│   ├── dynamodb/        # DynamoDB table
│   ├── ecr/             # Container registry
│   ├── ecs/             # ECS cluster and service
│   ├── alb/             # Application Load Balancer
│   ├── cloudfront/      # CloudFront distribution
│   ├── iam/             # IAM roles and policies
│   └── secrets/         # Secrets Manager
├── scripts/             # Helper scripts
├── main.tf              # Root module
├── variables.tf         # Input variables
├── outputs.tf           # Output values
├── providers.tf         # Provider configuration
├── backend.tf           # State backend configuration
└── versions.tf          # Version constraints
```

## Getting Started

### 1. Bootstrap Terraform Backend

First, create the S3 bucket and DynamoDB table for Terraform state:

```bash
# Make the script executable
chmod +x terraform/scripts/bootstrap.sh

# Run the bootstrap script
./terraform/scripts/bootstrap.sh
```

### 2. Initialize Terraform

```bash
cd terraform/environments/dev  # or prod

terraform init
```

### 3. Review the Plan

```bash
terraform plan
```

### 4. Apply the Configuration

```bash
terraform apply
```

## Environment Configuration

### Development (dev)

Optimized for cost savings:
- Single NAT Gateway
- Smaller RDS instance (db.t3.micro)
- Single ECS task
- On-demand DynamoDB billing

### Production (prod)

Optimized for reliability:
- Multi-AZ NAT Gateways
- Larger RDS instance (db.r6g.large) with Multi-AZ
- Multiple ECS tasks with auto-scaling
- Provisioned DynamoDB capacity

## Deployment

### Manual Deployment

1. Build and push Docker image:
```bash
cd backend

# Build the JAR
mvn clean package -DskipTests

# Build Docker image
docker build -t document-service .

# Tag and push to ECR
aws ecr get-login-password --region ap-south-1 | docker login --username AWS --password-stdin <account-id>.dkr.ecr.ap-south-1.amazonaws.com
docker tag document-service:latest <ecr-repository-url>:latest
docker push <ecr-repository-url>:latest
```

2. Update ECS service:
```bash
aws ecs update-service --cluster document-service-dev-cluster --service document-service-dev-service --force-new-deployment
```

3. Deploy frontend:
```bash
cd frontend
npm run build
aws s3 sync build/ s3://<frontend-bucket-name>/ --delete
aws cloudfront create-invalidation --distribution-id <distribution-id> --paths "/*"
```

### CI/CD Deployment

The GitHub Actions workflows handle deployment automatically:
- Push to `develop` branch → Deploy to dev environment
- Push to `main` branch → Deploy to prod environment

## Outputs

After applying Terraform, you'll get these outputs:

| Output | Description |
|--------|-------------|
| `cloudfront_domain_name` | URL to access the application |
| `alb_dns_name` | Direct API endpoint |
| `ecr_repository_url` | ECR URL for pushing images |
| `s3_documents_bucket` | S3 bucket for documents |
| `s3_frontend_bucket` | S3 bucket for frontend |
| `dynamodb_table_name` | DynamoDB table name |

## GitHub Secrets Required

Configure these secrets in your GitHub repository:

| Secret | Description |
|--------|-------------|
| `AWS_ACCESS_KEY_ID` | AWS access key |
| `AWS_SECRET_ACCESS_KEY` | AWS secret key |
| `DEV_API_URL` | Dev CloudFront URL (e.g., https://xxx.cloudfront.net) |
| `DEV_FRONTEND_BUCKET` | Dev S3 bucket name |
| `DEV_CLOUDFRONT_ID` | Dev CloudFront distribution ID |
| `PROD_API_URL` | Prod CloudFront URL |
| `PROD_FRONTEND_BUCKET` | Prod S3 bucket name |
| `PROD_CLOUDFRONT_ID` | Prod CloudFront distribution ID |

## pgvector Setup

After RDS is created, connect to the database and enable pgvector:

```sql
CREATE EXTENSION IF NOT EXISTS vector;
```

The application's JPA configuration will handle table creation automatically.

## Cost Estimation

| Environment | Monthly Cost (approx) |
|------------|----------------------|
| Development | ~$90 |
| Production | ~$350 |

Major cost drivers:
- NAT Gateway ($35-70/month)
- RDS instance ($15-150/month)
- ECS Fargate ($15-60/month)

## Troubleshooting

### ECS Tasks Not Starting

1. Check CloudWatch logs: `/ecs/document-service-{env}`
2. Verify secrets are accessible
3. Check security group rules

### Database Connection Issues

1. Verify RDS security group allows traffic from ECS security group
2. Check the database endpoint in Secrets Manager
3. Ensure pgvector extension is enabled

### Frontend Not Loading

1. Check S3 bucket policy
2. Verify CloudFront origin access control
3. Invalidate CloudFront cache

## Cleanup

To destroy all resources:

```bash
cd terraform/environments/dev  # or prod
terraform destroy
```

**Warning**: This will delete all data including databases and S3 objects!


