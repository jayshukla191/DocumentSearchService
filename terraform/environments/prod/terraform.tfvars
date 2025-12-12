# Production Environment Variables
# These are optimized for production workloads

environment    = "prod"
aws_region     = "ap-south-1"
project_name   = "document-service"

# VPC Configuration (different CIDR from dev to allow VPC peering if needed)
vpc_cidr             = "10.1.0.0/16"
availability_zones   = ["ap-south-1a", "ap-south-1b"]
public_subnet_cidrs  = ["10.1.1.0/24", "10.1.2.0/24"]
private_subnet_cidrs = ["10.1.10.0/24", "10.1.11.0/24"]

# RDS Configuration - Production-grade instance
db_instance_class    = "db.r6g.large"  # 2 vCPU, 16 GB RAM
db_allocated_storage = 100              # 100 GB with auto-scaling to 200 GB
db_name              = "document_management"
db_username          = "postgres"
db_multi_az          = true             # High availability

# ECS Configuration - Production capacity
ecs_task_cpu      = 1024  # 1 vCPU
ecs_task_memory   = 2048  # 2 GB
ecs_desired_count = 2     # 2 tasks for HA
ecs_min_capacity  = 2     # Minimum 2 for HA
ecs_max_capacity  = 6     # Can scale to 6 tasks

# DynamoDB - Provisioned capacity for predictable performance
dynamodb_billing_mode   = "PROVISIONED"
dynamodb_read_capacity  = 25
dynamodb_write_capacity = 25

# NAT Gateway - Multiple for high availability
enable_nat_gateway = true
single_nat_gateway = false  # One NAT per AZ for HA

# Bedrock Configuration
bedrock_region          = "us-east-1"
bedrock_embedding_model = "amazon.titan-embed-text-v2:0"
bedrock_llm_model       = "amazon.titan-text-lite-v1"


