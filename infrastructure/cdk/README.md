# Infrastructure as Code - AWS CDK (Java)

This directory contains the AWS CDK infrastructure code for the Document Management Service.

## Prerequisites

- Java 17+
- Maven 3.6+
- AWS CLI configured (`aws configure`)
- AWS CDK CLI installed: `npm install -g aws-cdk`
- Docker (for building and testing)

## Setup

1. **Install CDK CLI** (if not already installed):
   ```bash
   npm install -g aws-cdk
   ```

2. **Bootstrap CDK** (first time only):
   ```bash
   cdk bootstrap aws://ACCOUNT-ID/REGION
   ```

3. **Build the project**:
   ```bash
   mvn clean compile
   ```

4. **Synthesize CloudFormation template**:
   ```bash
   cdk synth
   ```

## Deployment

### Deploy to Development Environment

```bash
cdk deploy DocumentServiceStack-dev --context environment=dev
```

### Deploy to Production Environment

```bash
cdk deploy DocumentServiceStack-prod --context environment=prod
```

### Deploy with Custom Domain

```bash
cdk deploy DocumentServiceStack-prod \
  --context environment=prod \
  --context domainName=api.yourdomain.com
```

## Useful Commands

- `mvn compile` - Compile Java code
- `cdk synth` - Synthesize CloudFormation template
- `cdk deploy` - Deploy stack to AWS
- `cdk diff` - Compare deployed stack with current state
- `cdk destroy` - Destroy stack
- `cdk list` - List all stacks
- `cdk doctor` - Check CDK setup

## Project Structure

```
infrastructure/cdk/
├── pom.xml                          # Maven dependencies
├── cdk.json                         # CDK configuration
├── src/main/java/
│   └── com/docmanagement/infrastructure/
│       ├── App.java                 # CDK app entry point
│       └── DocumentServiceStack.java # Main stack definition
└── README.md                        # This file
```

## Environment Configuration

Environments are configured via CDK context:

- `environment`: dev, staging, prod
- `domainName`: Optional custom domain name

## Post-Deployment Steps

After deploying the infrastructure:

1. **Install pgvector extension**:
   ```sql
   psql -h <rds-endpoint> -U postgres -d document_management
   CREATE EXTENSION vector;
   ```

2. **Update ECS task definition** with your Docker image:
   - The stack uses a placeholder image
   - Update via CI/CD pipeline or manually

3. **Configure SSL certificate** (if using custom domain):
   - Request certificate in ACM
   - Update ALB listener to use HTTPS

## CI/CD Integration

See `.github/workflows/deploy-infrastructure-cdk.yml` for automated deployment via GitHub Actions.

## Cost Estimation

- Development: ~$50-100/month
- Production: ~$600-800/month

See `AWS_DEPLOYMENT_GUIDE.md` for detailed cost breakdown.
