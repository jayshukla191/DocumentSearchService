################################################################################
# Production Environment Configuration
################################################################################

terraform {
  required_version = ">= 1.5.0"

  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.0"
    }
    random = {
      source  = "hashicorp/random"
      version = "~> 3.5"
    }
  }

  # Backend configuration for state storage
  # Uncomment and configure for remote state
  # backend "s3" {
  #   bucket         = "your-terraform-state-bucket"
  #   key            = "document-service/prod/terraform.tfstate"
  #   region         = "us-east-1"
  #   encrypt        = true
  #   dynamodb_table = "terraform-state-lock"
  # }
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

################################################################################
# Call Root Module
################################################################################

module "infrastructure" {
  source = "../../"

  # General
  project_name = var.project_name
  environment  = var.environment
  aws_region   = var.aws_region

  # VPC
  vpc_cidr             = var.vpc_cidr
  availability_zones   = var.availability_zones
  public_subnet_cidrs  = var.public_subnet_cidrs
  private_subnet_cidrs = var.private_subnet_cidrs
  enable_nat_gateway   = var.enable_nat_gateway
  single_nat_gateway   = var.single_nat_gateway

  # RDS
  db_instance_class          = var.db_instance_class
  db_allocated_storage       = var.db_allocated_storage
  db_max_allocated_storage   = var.db_max_allocated_storage
  db_name                    = var.db_name
  db_username                = var.db_username
  db_multi_az                = var.db_multi_az
  db_backup_retention_period = var.db_backup_retention_period
  db_deletion_protection     = var.db_deletion_protection

  # ECS
  ecs_task_cpu     = var.ecs_task_cpu
  ecs_task_memory  = var.ecs_task_memory
  ecs_desired_count = var.ecs_desired_count
  ecs_min_capacity = var.ecs_min_capacity
  ecs_max_capacity = var.ecs_max_capacity
  container_port   = var.container_port
  health_check_path = var.health_check_path
  backend_image    = var.backend_image

  # S3
  s3_versioning_enabled     = var.s3_versioning_enabled
  s3_lifecycle_glacier_days = var.s3_lifecycle_glacier_days

  # DynamoDB
  dynamodb_billing_mode  = var.dynamodb_billing_mode
  dynamodb_read_capacity = var.dynamodb_read_capacity
  dynamodb_write_capacity = var.dynamodb_write_capacity

  # CloudFront
  enable_cloudfront    = var.enable_cloudfront
  cloudfront_price_class = var.cloudfront_price_class

  # Monitoring
  alarm_email               = var.alarm_email
  enable_enhanced_monitoring = var.enable_enhanced_monitoring

  # CI/CD
  enable_codepipeline = var.enable_codepipeline
  github_repository   = var.github_repository
  github_branch       = var.github_branch

  # Domain
  domain_name           = var.domain_name
  create_route53_records = var.create_route53_records

  tags = var.tags
}
