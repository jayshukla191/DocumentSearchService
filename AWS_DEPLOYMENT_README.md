# AWS Deployment Documentation

This directory contains comprehensive documentation and configuration files for deploying the Document Management Service to AWS.

## 📁 Files Overview

### 📘 Main Documentation

1. **`AWS_DEPLOYMENT_GUIDE.md`** ⭐ **START HERE**
   - Comprehensive guide covering all AWS services needed
   - Architecture diagrams and recommendations
   - Detailed service configurations
   - Cost estimations for different scales
   - Security best practices
   - Step-by-step deployment instructions

2. **`AWS_DEPLOYMENT_CHECKLIST.md`**
   - Step-by-step checklist for deployment
   - Pre-deployment, infrastructure, and post-deployment tasks
   - Quick command reference
   - Testing and validation checklist

3. **`AWS_QUICK_START.md`**
   - Condensed quick start guide
   - 5-step deployment process
   - Common commands and troubleshooting
   - Essential monitoring setup

### 🐳 Docker & Container Files

4. **`backend/Dockerfile`**
   - Multi-stage Docker build for Spring Boot backend
   - Optimized for production with security best practices
   - Includes health check configuration

5. **`backend/.dockerignore`**
   - Excludes unnecessary files from Docker build context

6. **`frontend/Dockerfile`**
   - Multi-stage build for React frontend
   - Uses Nginx for serving static files
   - Production-optimized

7. **`frontend/nginx.conf`**
   - Nginx configuration for frontend
   - Gzip compression, caching, security headers
   - SPA routing support

8. **`docker-compose.yml`**
   - Local development setup
   - Includes PostgreSQL, backend, and frontend services
   - Useful for testing before AWS deployment

### ⚙️ AWS Configuration Files

9. **`backend/ecs-task-definition.json`**
   - ECS Fargate task definition template
   - Includes environment variables and secrets configuration
   - Health check and logging setup
   - **Update with your AWS account ID and region**

10. **`aws-iam-policies.json`**
    - IAM policy examples for:
      - ECS Task Execution Role
      - ECS Task Role (S3, DynamoDB, Bedrock access)
      - CodeBuild Role
    - Use these to create IAM roles in AWS Console

## 🚀 Getting Started

### For First-Time AWS Deployment:

1. **Read**: `AWS_DEPLOYMENT_GUIDE.md` - Understand the architecture
2. **Follow**: `AWS_DEPLOYMENT_CHECKLIST.md` - Step-by-step deployment
3. **Reference**: `AWS_QUICK_START.md` - Quick commands and troubleshooting

### For Quick Deployment:

1. **Read**: `AWS_QUICK_START.md` - 5-step process
2. **Use**: `docker-compose.yml` - Test locally first
3. **Deploy**: Follow checklist items

## 🏗️ Architecture Options

### Option 1: ECS Fargate (Recommended for Production)
- **Best for**: Production workloads, auto-scaling needs
- **Pros**: Serverless containers, no EC2 management, auto-scaling
- **Cons**: Slightly higher cost than EC2
- **Files**: `backend/Dockerfile`, `backend/ecs-task-definition.json`

### Option 2: Elastic Beanstalk
- **Best for**: Quick deployment, less container experience needed
- **Pros**: Easy deployment, built-in load balancing
- **Cons**: Less control, vendor lock-in
- **Files**: Use standard Spring Boot JAR deployment

### Option 3: EC2 (Traditional)
- **Best for**: Full control, predictable workloads
- **Pros**: Cost-effective, full control
- **Cons**: Requires EC2 management, manual scaling
- **Files**: `backend/Dockerfile` (can run on EC2)

## 📋 Prerequisites

Before starting deployment, ensure you have:

- [ ] AWS account with admin access
- [ ] AWS CLI installed and configured (`aws configure`)
- [ ] Docker installed (for building images)
- [ ] Domain name (optional, for custom domain)
- [ ] Bedrock model access enabled (request in AWS Console)

## 🔧 Required AWS Services Summary

| Service | Purpose | Required? |
|---------|---------|-----------|
| **ECS Fargate** | Container hosting | ✅ Yes |
| **RDS PostgreSQL** | Database with pgvector | ✅ Yes |
| **S3** | Document storage | ✅ Yes |
| **DynamoDB** | Metadata storage | ✅ Yes |
| **Bedrock** | AI/ML (embeddings, LLM) | ✅ Yes |
| **VPC** | Networking | ✅ Yes |
| **ALB** | Load balancing | ✅ Yes |
| **CloudFront** | CDN (optional but recommended) | ⚠️ Recommended |
| **Route 53** | DNS (if using custom domain) | ⚠️ Optional |
| **Secrets Manager** | Credential storage | ✅ Yes |
| **CloudWatch** | Monitoring | ✅ Yes |
| **IAM** | Access control | ✅ Yes |
| **ACM** | SSL certificates | ✅ Yes (for HTTPS) |

## 💰 Cost Estimates

| Scale | Monthly Cost | Use Case |
|-------|--------------|----------|
| Development | $50-100 | Testing, development |
| Small Production | $150-250 | Low traffic, <1000 users |
| Medium Production | $600-800 | Moderate traffic, <10K users |
| Large Production | $2,000-3,000 | High traffic, >10K users |

*See `AWS_DEPLOYMENT_GUIDE.md` for detailed cost breakdown*

## 🔐 Security Checklist

- [ ] Use IAM roles (not access keys in code)
- [ ] Store secrets in AWS Secrets Manager
- [ ] Enable encryption at rest (RDS, S3, DynamoDB)
- [ ] Enable SSL/TLS (HTTPS only)
- [ ] Restrict security groups (minimum ports)
- [ ] Use private subnets for databases
- [ ] Enable CloudTrail for audit logging
- [ ] Configure WAF rules (optional)

## 📊 Monitoring Setup

Essential CloudWatch metrics to monitor:

1. **ECS**: CPU, Memory, Task Count
2. **RDS**: CPU, Connections, Storage
3. **ALB**: Request Count, Error Rate, Latency
4. **S3**: Bucket Size, Request Count
5. **DynamoDB**: Read/Write Capacity, Throttling

See `AWS_DEPLOYMENT_GUIDE.md` for detailed monitoring setup.

## 🛠️ Local Testing

Before deploying to AWS, test locally:

```bash
# Start all services locally
docker-compose up -d

# Test backend
curl http://localhost:8080/actuator/health

# Test frontend
open http://localhost:3000
```

## 📝 Next Steps After Deployment

1. **Set up CI/CD**: Configure CodePipeline for automated deployments
2. **Enable Monitoring**: Create CloudWatch dashboards and alarms
3. **Optimize Costs**: Review instance sizes and usage
4. **Set up Backups**: Configure automated backups for RDS
5. **Document Runbooks**: Create operational procedures

## 🆘 Troubleshooting

Common issues and solutions:

- **Backend won't start**: Check ECS logs, security groups, Secrets Manager permissions
- **Database connection fails**: Verify RDS security group, subnet configuration
- **Frontend can't reach API**: Check CORS, ALB target group health, security groups
- **High costs**: Review instance sizes, enable auto-scaling, optimize S3 lifecycle

See `AWS_QUICK_START.md` for detailed troubleshooting.

## 📚 Additional Resources

- [AWS Well-Architected Framework](https://aws.amazon.com/architecture/well-architected/)
- [AWS Pricing Calculator](https://calculator.aws/)
- [ECS Best Practices](https://docs.aws.amazon.com/AmazonECS/latest/bestpracticesguide/)
- [RDS PostgreSQL Guide](https://docs.aws.amazon.com/AmazonRDS/latest/UserGuide/CHAP_GettingStarted.CreatingConnecting.PostgreSQL.html)

## 🤝 Support

For issues or questions:
1. Check the troubleshooting sections in the guides
2. Review AWS documentation
3. Check CloudWatch logs
4. Verify IAM permissions and security groups

---

**Last Updated**: 2024
**Version**: 1.0
