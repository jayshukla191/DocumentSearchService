# Main Terraform configuration that orchestrates all modules

# Random string for unique resource naming
resource "random_string" "suffix" {
  length  = 8
  special = false
  upper   = false
}

locals {
  name_prefix = "${var.project_name}-${var.environment}"
  common_tags = {
    Project     = var.project_name
    Environment = var.environment
    ManagedBy   = "terraform"
  }
}

# VPC Module
module "vpc" {
  source = "./modules/vpc"

  name_prefix          = local.name_prefix
  vpc_cidr             = var.vpc_cidr
  availability_zones   = var.availability_zones
  public_subnet_cidrs  = var.public_subnet_cidrs
  private_subnet_cidrs = var.private_subnet_cidrs
  enable_nat_gateway   = var.enable_nat_gateway
  single_nat_gateway   = var.single_nat_gateway

  tags = local.common_tags
}

# Security Module (IAM Roles)
module "iam" {
  source = "./modules/iam"

  name_prefix       = local.name_prefix
  aws_region        = var.aws_region
  bedrock_region    = var.bedrock_region
  s3_bucket_arn     = module.s3.documents_bucket_arn
  dynamodb_table_arn = module.dynamodb.table_arn

  tags = local.common_tags
}

# Secrets Manager Module
module "secrets" {
  source = "./modules/secrets"

  name_prefix = local.name_prefix
  db_username = var.db_username

  tags = local.common_tags
}

# RDS Module
module "rds" {
  source = "./modules/rds"

  name_prefix           = local.name_prefix
  vpc_id                = module.vpc.vpc_id
  private_subnet_ids    = module.vpc.private_subnet_ids
  ecs_security_group_id = module.vpc.ecs_security_group_id
  
  instance_class         = var.db_instance_class
  allocated_storage      = var.db_allocated_storage
  db_name                = var.db_name
  db_username            = var.db_username
  db_password_secret_arn = module.secrets.db_password_secret_arn
  multi_az               = var.db_multi_az

  tags = local.common_tags

  depends_on = [module.secrets]
}

# S3 Module
module "s3" {
  source = "./modules/s3"

  name_prefix   = local.name_prefix
  random_suffix = random_string.suffix.result

  tags = local.common_tags
}

# DynamoDB Module
module "dynamodb" {
  source = "./modules/dynamodb"

  name_prefix    = local.name_prefix
  billing_mode   = var.dynamodb_billing_mode
  read_capacity  = var.dynamodb_read_capacity
  write_capacity = var.dynamodb_write_capacity

  tags = local.common_tags
}

# ECR Module
module "ecr" {
  source = "./modules/ecr"

  name_prefix = local.name_prefix

  tags = local.common_tags
}

# ALB Module
module "alb" {
  source = "./modules/alb"

  name_prefix          = local.name_prefix
  vpc_id               = module.vpc.vpc_id
  public_subnet_ids    = module.vpc.public_subnet_ids
  alb_security_group_id = module.vpc.alb_security_group_id
  container_port       = var.container_port
  health_check_path    = var.health_check_path

  tags = local.common_tags
}

# ECS Module
module "ecs" {
  source = "./modules/ecs"

  name_prefix           = local.name_prefix
  aws_region            = var.aws_region
  vpc_id                = module.vpc.vpc_id
  private_subnet_ids    = module.vpc.private_subnet_ids
  ecs_security_group_id = module.vpc.ecs_security_group_id
  
  ecr_repository_url      = module.ecr.repository_url
  task_execution_role_arn = module.iam.ecs_task_execution_role_arn
  task_role_arn           = module.iam.ecs_task_role_arn
  
  target_group_arn      = module.alb.target_group_arn
  
  task_cpu              = var.ecs_task_cpu
  task_memory           = var.ecs_task_memory
  desired_count         = var.ecs_desired_count
  min_capacity          = var.ecs_min_capacity
  max_capacity          = var.ecs_max_capacity
  container_port        = var.container_port
  
  # Environment variables for the application
  db_host                = module.rds.address
  db_name                = var.db_name
  db_username            = var.db_username
  db_password_secret_arn = module.secrets.db_password_secret_arn
  jwt_secret_arn         = module.secrets.jwt_secret_arn
  s3_bucket_name         = module.s3.documents_bucket_name
  dynamodb_table_name    = module.dynamodb.table_name
  bedrock_region         = var.bedrock_region
  bedrock_embedding_model = var.bedrock_embedding_model
  bedrock_llm_model      = var.bedrock_llm_model

  tags = local.common_tags

  depends_on = [module.alb, module.rds]
}

# CloudFront Module
module "cloudfront" {
  source = "./modules/cloudfront"

  name_prefix              = local.name_prefix
  frontend_bucket_id       = module.s3.frontend_bucket_id
  frontend_bucket_arn      = module.s3.frontend_bucket_arn
  frontend_bucket_domain   = module.s3.frontend_bucket_regional_domain_name
  alb_dns_name             = module.alb.alb_dns_name

  tags = local.common_tags
}

