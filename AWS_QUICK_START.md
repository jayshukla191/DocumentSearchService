# AWS Quick Start Guide

## 🚀 Quick Deployment Summary

This guide provides a condensed overview of deploying your Document Management Service to AWS. For detailed information, see `AWS_DEPLOYMENT_GUIDE.md`.

---

## 📦 What You Need

### Core AWS Services:
1. **Compute**: ECS Fargate (or Elastic Beanstalk)
2. **Database**: RDS PostgreSQL with pgvector
3. **Storage**: S3 (documents) + DynamoDB (metadata)
4. **AI/ML**: Bedrock (Titan models)
5. **Networking**: VPC, ALB, CloudFront
6. **Security**: IAM, Secrets Manager, ACM
7. **Monitoring**: CloudWatch

---

## 🎯 Recommended Architecture

**For Production**: ECS Fargate + RDS Multi-AZ + ALB + CloudFront
**For Development**: Elastic Beanstalk + RDS Single-AZ

---

## ⚡ Quick Deployment (5 Steps)

### Step 1: Set Up Infrastructure (30 minutes)

```bash
# 1. Create VPC with public/private subnets
# 2. Create RDS PostgreSQL instance
aws rds create-db-instance \
  --db-instance-identifier document-db \
  --db-instance-class db.t3.micro \
  --engine postgres \
  --engine-version 14.10 \
  --master-username postgres \
  --master-user-password <password> \
  --allocated-storage 20

# 3. Create S3 buckets
aws s3 mb s3://document-service-files-prod
aws s3 mb s3://document-service-frontend-prod

# 4. Create DynamoDB table
aws dynamodb create-table \
  --table-name DocumentMetadata \
  --attribute-definitions AttributeName=DocumentId,AttributeType=S \
  --key-schema AttributeName=DocumentId,KeyType=HASH \
  --billing-mode PAY_PER_REQUEST
```

### Step 2: Build & Push Docker Image (10 minutes)

```bash
# Build Docker image
cd backend
docker build -t document-service-backend .

# Create ECR repository
aws ecr create-repository --repository-name document-service-backend

# Login and push
aws ecr get-login-password --region us-east-1 | \
  docker login --username AWS --password-stdin <account-id>.dkr.ecr.us-east-1.amazonaws.com

docker tag document-service-backend:latest \
  <account-id>.dkr.ecr.us-east-1.amazonaws.com/document-service-backend:latest

docker push <account-id>.dkr.ecr.us-east-1.amazonaws.com/document-service-backend:latest
```

### Step 3: Deploy Backend to ECS (15 minutes)

```bash
# Create ECS cluster
aws ecs create-cluster --cluster-name document-service-cluster

# Register task definition (update ecs-task-definition.json with your values)
aws ecs register-task-definition --cli-input-json file://backend/ecs-task-definition.json

# Create ECS service
aws ecs create-service \
  --cluster document-service-cluster \
  --service-name document-service-backend \
  --task-definition document-service-backend \
  --desired-count 2 \
  --launch-type FARGATE \
  --network-configuration "awsvpcConfiguration={subnets=[subnet-xxx,subnet-yyy],securityGroups=[sg-xxx],assignPublicIp=DISABLED}" \
  --load-balancers "targetGroupArn=arn:aws:elasticloadbalancing:region:account:targetgroup/xxx,containerName=document-service-backend,containerPort=8080"
```

### Step 4: Deploy Frontend (5 minutes)

```bash
cd frontend
npm install
npm run build
aws s3 sync build/ s3://document-service-frontend-prod --delete
```

### Step 5: Set Up Load Balancer & DNS (10 minutes)

```bash
# Create Application Load Balancer (via console recommended)
# Configure target group pointing to ECS service
# Attach SSL certificate from ACM
# Create CloudFront distribution (optional)
# Configure Route 53 DNS records
```

---

## 💰 Estimated Monthly Costs

| Environment | Monthly Cost |
|------------|--------------|
| **Development** | $50-100 |
| **Small Production** | $150-250 |
| **Medium Production** | $600-800 |
| **Large Production** | $2,000-3,000 |

*Costs vary by region, usage, and instance sizes*

---

## 🔐 Security Checklist

- [ ] Use IAM roles (not access keys)
- [ ] Store secrets in Secrets Manager
- [ ] Enable encryption at rest (RDS, S3, DynamoDB)
- [ ] Enable SSL/TLS (HTTPS only)
- [ ] Restrict security groups (minimum required ports)
- [ ] Enable WAF (optional but recommended)
- [ ] Use private subnets for databases
- [ ] Enable CloudTrail for audit logging

---

## 📊 Monitoring Setup

### Essential CloudWatch Alarms:
1. **High CPU** (>80%) - ECS tasks
2. **High Memory** (>85%) - ECS tasks  
3. **High Error Rate** (>5%) - ALB
4. **Database Connections** (>80% of max) - RDS
5. **Low Storage** (<20% free) - RDS

### Log Groups:
- `/ecs/document-service-backend` - Application logs
- `/aws/rds/postgresql` - Database logs (if enabled)

---

## 🛠️ Common Commands

### Check Service Status
```bash
aws ecs describe-services \
  --cluster document-service-cluster \
  --services document-service-backend
```

### View Logs
```bash
aws logs tail /ecs/document-service-backend --follow
```

### Scale Service
```bash
aws ecs update-service \
  --cluster document-service-cluster \
  --service document-service-backend \
  --desired-count 5
```

### Update Application
```bash
# Build and push new image
docker build -t document-service-backend .
docker push <ecr-uri>:latest

# Force new deployment
aws ecs update-service \
  --cluster document-service-cluster \
  --service document-service-backend \
  --force-new-deployment
```

---

## 🐛 Troubleshooting

### Backend won't start
- Check ECS task logs: `aws logs tail /ecs/document-service-backend`
- Verify security groups allow traffic
- Check Secrets Manager permissions
- Verify RDS endpoint is correct

### Can't connect to database
- Check RDS security group allows ECS security group
- Verify RDS is in private subnet
- Check database credentials in Secrets Manager

### Frontend can't reach API
- Verify CORS configuration
- Check ALB target group health
- Verify security groups
- Check CloudFront origin settings

---

## 📚 Next Steps

1. **Read Full Guide**: See `AWS_DEPLOYMENT_GUIDE.md` for detailed architecture
2. **Follow Checklist**: Use `AWS_DEPLOYMENT_CHECKLIST.md` for step-by-step
3. **Set Up CI/CD**: Configure CodePipeline for automated deployments
4. **Optimize Costs**: Review and right-size instances
5. **Enable Monitoring**: Set up comprehensive CloudWatch dashboards

---

## 🔗 Useful Links

- [AWS Pricing Calculator](https://calculator.aws/)
- [ECS Documentation](https://docs.aws.amazon.com/ecs/)
- [RDS PostgreSQL Guide](https://docs.aws.amazon.com/AmazonRDS/latest/UserGuide/CHAP_GettingStarted.CreatingConnecting.PostgreSQL.html)
- [pgvector on RDS](https://github.com/pgvector/pgvector)

---

**Need Help?** Refer to the detailed deployment guide or AWS documentation.
