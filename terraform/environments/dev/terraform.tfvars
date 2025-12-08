################################################################################
# Development Environment - terraform.tfvars
# Customize these values for your development environment
################################################################################

project_name = "document-service"
environment  = "dev"
aws_region   = "us-east-1"

# VPC Configuration
vpc_cidr             = "10.0.0.0/16"
availability_zones   = ["us-east-1a", "us-east-1b"]
public_subnet_cidrs  = ["10.0.1.0/24", "10.0.2.0/24"]
private_subnet_cidrs = ["10.0.10.0/24", "10.0.11.0/24"]
enable_nat_gateway   = true
single_nat_gateway   = true  # Use single NAT Gateway to save costs

# RDS Configuration (Small for dev)
db_instance_class          = "db.t3.micro"
db_allocated_storage       = 20
db_max_allocated_storage   = 50
db_name                    = "document_management"
db_username                = "docadmin"
db_multi_az                = false
db_backup_retention_period = 7
db_deletion_protection     = false

# ECS Configuration (Small for dev)
ecs_task_cpu      = 512
ecs_task_memory   = 1024
ecs_desired_count = 1
ecs_min_capacity  = 1
ecs_max_capacity  = 2
container_port    = 8080
health_check_path = "/actuator/health"

# S3 Configuration
s3_versioning_enabled     = true
s3_lifecycle_glacier_days = 90

# DynamoDB Configuration (On-Demand for dev)
dynamodb_billing_mode = "PAY_PER_REQUEST"

# CloudFront (Disabled for dev to save costs)
enable_cloudfront    = false
cloudfront_price_class = "PriceClass_100"

# Monitoring
alarm_email               = ""  # Add your email for alerts
enable_enhanced_monitoring = false

# CI/CD (Disable for initial setup)
enable_codepipeline = false
github_repository   = ""  # e.g., "your-org/document-service"
github_branch       = "develop"

# Domain (Optional)
domain_name           = ""
create_route53_records = false

# Additional Tags
tags = {
  Team = "development"
  CostCenter = "dev-001"
}
