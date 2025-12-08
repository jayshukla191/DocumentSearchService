################################################################################
# Production Environment - terraform.tfvars
# Customize these values for your production environment
################################################################################

project_name = "document-service"
environment  = "prod"
aws_region   = "us-east-1"

# VPC Configuration
vpc_cidr             = "10.0.0.0/16"
availability_zones   = ["us-east-1a", "us-east-1b"]
public_subnet_cidrs  = ["10.0.1.0/24", "10.0.2.0/24"]
private_subnet_cidrs = ["10.0.10.0/24", "10.0.11.0/24"]
enable_nat_gateway   = true
single_nat_gateway   = false  # Use multiple NAT Gateways for high availability

# RDS Configuration (Production-grade)
db_instance_class          = "db.r6g.large"   # 2 vCPU, 16 GB RAM
db_allocated_storage       = 100
db_max_allocated_storage   = 500
db_name                    = "document_management"
db_username                = "docadmin"
db_multi_az                = true             # Enable Multi-AZ for high availability
db_backup_retention_period = 30
db_deletion_protection     = true             # Prevent accidental deletion

# ECS Configuration (Production-grade)
ecs_task_cpu      = 1024   # 1 vCPU
ecs_task_memory   = 2048   # 2 GB
ecs_desired_count = 3      # High availability
ecs_min_capacity  = 2
ecs_max_capacity  = 10
container_port    = 8080
health_check_path = "/actuator/health"

# S3 Configuration
s3_versioning_enabled     = true
s3_lifecycle_glacier_days = 90

# DynamoDB Configuration (Provisioned with auto-scaling)
dynamodb_billing_mode   = "PROVISIONED"
dynamodb_read_capacity  = 100
dynamodb_write_capacity = 100

# CloudFront (Enabled for production)
enable_cloudfront    = true
cloudfront_price_class = "PriceClass_All"  # All edge locations worldwide

# Monitoring (Enhanced monitoring enabled)
alarm_email               = ""  # IMPORTANT: Add your email for production alerts
enable_enhanced_monitoring = true

# CI/CD (Enable for automated deployments)
enable_codepipeline = true
github_repository   = ""  # REQUIRED: Set to your GitHub repository, e.g., "your-org/document-service"
github_branch       = "main"

# Domain (Configure for your custom domain)
domain_name           = ""  # Set your domain, e.g., "docs.example.com"
create_route53_records = false  # Set to true if using Route 53

# Additional Tags
tags = {
  Team        = "platform"
  CostCenter  = "prod-001"
  Compliance  = "required"
  DataClass   = "confidential"
}
