# Development Environment Variables
# These are the default values optimized for development

environment    = "dev"
aws_region     = "ap-south-1"
project_name   = "document-service"

# VPC Configuration
vpc_cidr             = "10.0.0.0/16"
availability_zones   = ["ap-south-1a", "ap-south-1b"]
public_subnet_cidrs  = ["10.0.1.0/24", "10.0.2.0/24"]
private_subnet_cidrs = ["10.0.10.0/24", "10.0.11.0/24"]

# RDS Configuration - Small instance for dev
db_instance_class    = "db.t3.micro"
db_allocated_storage = 20
db_name              = "document_management"
db_username          = "postgres"
db_multi_az          = false

# ECS Configuration - Minimal for dev
ecs_task_cpu      = 512   # 0.5 vCPU
ecs_task_memory   = 1024  # 1 GB
ecs_desired_count = 1
ecs_min_capacity  = 1
ecs_max_capacity  = 2

# DynamoDB - On-demand for dev (pay per request)
dynamodb_billing_mode = "PAY_PER_REQUEST"

# NAT Gateway - Single for cost saving in dev
enable_nat_gateway = true
single_nat_gateway = true

# Bedrock Configuration
bedrock_region          = "us-east-1"
bedrock_embedding_model = "amazon.titan-embed-text-v2:0"
bedrock_llm_model       = "amazon.titan-text-lite-v1"


