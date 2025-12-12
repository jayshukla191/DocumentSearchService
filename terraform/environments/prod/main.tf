# Production Environment Configuration

terraform {
  required_version = ">= 1.5.0"

  backend "s3" {
    bucket         = "document-service-terraform-state"
    key            = "environments/prod/terraform.tfstate"
    region         = "ap-south-1"
    encrypt        = true
    dynamodb_table = "document-service-terraform-locks"
  }
}

provider "aws" {
  region = var.aws_region

  default_tags {
    tags = {
      Project     = var.project_name
      Environment = var.environment
      ManagedBy   = "terraform"
    }
  }
}

provider "aws" {
  alias  = "us_east_1"
  region = "us-east-1"

  default_tags {
    tags = {
      Project     = var.project_name
      Environment = var.environment
      ManagedBy   = "terraform"
    }
  }
}

# Call the root module
module "document_service" {
  source = "../../"

  environment    = var.environment
  aws_region     = var.aws_region
  project_name   = var.project_name

  # VPC Configuration
  vpc_cidr             = var.vpc_cidr
  availability_zones   = var.availability_zones
  public_subnet_cidrs  = var.public_subnet_cidrs
  private_subnet_cidrs = var.private_subnet_cidrs

  # RDS Configuration - larger for prod
  db_instance_class    = var.db_instance_class
  db_allocated_storage = var.db_allocated_storage
  db_name              = var.db_name
  db_username          = var.db_username
  db_multi_az          = var.db_multi_az

  # ECS Configuration - larger for prod
  ecs_task_cpu      = var.ecs_task_cpu
  ecs_task_memory   = var.ecs_task_memory
  ecs_desired_count = var.ecs_desired_count
  ecs_min_capacity  = var.ecs_min_capacity
  ecs_max_capacity  = var.ecs_max_capacity

  # DynamoDB Configuration - provisioned for prod
  dynamodb_billing_mode  = var.dynamodb_billing_mode
  dynamodb_read_capacity  = var.dynamodb_read_capacity
  dynamodb_write_capacity = var.dynamodb_write_capacity

  # NAT Gateway - HA for prod
  enable_nat_gateway = var.enable_nat_gateway
  single_nat_gateway = var.single_nat_gateway

  # Bedrock Configuration
  bedrock_region          = var.bedrock_region
  bedrock_embedding_model = var.bedrock_embedding_model
  bedrock_llm_model       = var.bedrock_llm_model
}


