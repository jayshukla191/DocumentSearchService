################################################################################
# Main Terraform Configuration
# This file orchestrates all modules for the Document Service infrastructure
################################################################################

locals {
  common_tags = merge(var.tags, {
    Project     = var.project_name
    Environment = var.environment
    ManagedBy   = "terraform"
  })

  name_prefix = "${var.project_name}-${var.environment}"
}

################################################################################
# VPC Module
################################################################################

module "vpc" {
  source = "./modules/vpc"

  project_name         = var.project_name
  environment          = var.environment
  vpc_cidr             = var.vpc_cidr
  availability_zones   = var.availability_zones
  public_subnet_cidrs  = var.public_subnet_cidrs
  private_subnet_cidrs = var.private_subnet_cidrs
  enable_nat_gateway   = var.enable_nat_gateway
  single_nat_gateway   = var.single_nat_gateway

  tags = local.common_tags
}

################################################################################
# Security Groups Module
################################################################################

module "security_groups" {
  source = "./modules/security-groups"

  project_name   = var.project_name
  environment    = var.environment
  vpc_id         = module.vpc.vpc_id
  vpc_cidr       = var.vpc_cidr
  container_port = var.container_port

  tags = local.common_tags
}

################################################################################
# IAM Module
################################################################################

module "iam" {
  source = "./modules/iam"

  project_name         = var.project_name
  environment          = var.environment
  aws_region           = var.aws_region
  documents_bucket_arn = module.s3.documents_bucket_arn
  dynamodb_table_arn   = module.dynamodb.table_arn

  tags = local.common_tags
}

################################################################################
# S3 Module
################################################################################

module "s3" {
  source = "./modules/s3"

  project_name              = var.project_name
  environment               = var.environment
  versioning_enabled        = var.s3_versioning_enabled
  lifecycle_glacier_days    = var.s3_lifecycle_glacier_days
  cloudfront_distribution_arn = var.enable_cloudfront ? module.cloudfront[0].distribution_arn : null

  tags = local.common_tags
}

################################################################################
# DynamoDB Module
################################################################################

module "dynamodb" {
  source = "./modules/dynamodb"

  project_name   = var.project_name
  environment    = var.environment
  billing_mode   = var.dynamodb_billing_mode
  read_capacity  = var.dynamodb_read_capacity
  write_capacity = var.dynamodb_write_capacity

  tags = local.common_tags
}

################################################################################
# RDS Module
################################################################################

module "rds" {
  source = "./modules/rds"

  project_name            = var.project_name
  environment             = var.environment
  vpc_id                  = module.vpc.vpc_id
  subnet_ids              = module.vpc.private_subnet_ids
  security_group_id       = module.security_groups.rds_security_group_id
  instance_class          = var.db_instance_class
  allocated_storage       = var.db_allocated_storage
  max_allocated_storage   = var.db_max_allocated_storage
  database_name           = var.db_name
  master_username         = var.db_username
  multi_az                = var.db_multi_az
  backup_retention_period = var.db_backup_retention_period
  deletion_protection     = var.db_deletion_protection
  enable_enhanced_monitoring = var.enable_enhanced_monitoring

  tags = local.common_tags
}

################################################################################
# ALB Module
################################################################################

module "alb" {
  source = "./modules/alb"

  project_name      = var.project_name
  environment       = var.environment
  vpc_id            = module.vpc.vpc_id
  public_subnet_ids = module.vpc.public_subnet_ids
  security_group_id = module.security_groups.alb_security_group_id
  container_port    = var.container_port
  health_check_path = var.health_check_path

  tags = local.common_tags
}

################################################################################
# ECS Module
################################################################################

module "ecs" {
  source = "./modules/ecs"

  project_name       = var.project_name
  environment        = var.environment
  aws_region         = var.aws_region
  vpc_id             = module.vpc.vpc_id
  private_subnet_ids = module.vpc.private_subnet_ids
  security_group_id  = module.security_groups.ecs_security_group_id
  
  # Task configuration
  task_cpu           = var.ecs_task_cpu
  task_memory        = var.ecs_task_memory
  container_port     = var.container_port
  backend_image      = var.backend_image
  
  # Service configuration
  desired_count      = var.ecs_desired_count
  min_capacity       = var.ecs_min_capacity
  max_capacity       = var.ecs_max_capacity
  
  # IAM roles
  execution_role_arn = module.iam.ecs_execution_role_arn
  task_role_arn      = module.iam.ecs_task_role_arn
  
  # ALB
  target_group_arn   = module.alb.target_group_arn
  
  # Environment variables for the container
  db_host            = module.rds.db_endpoint
  db_port            = module.rds.db_port
  db_name            = module.rds.db_name
  db_username        = var.db_username
  db_password_secret_arn = module.rds.db_password_secret_arn
  s3_bucket_name     = module.s3.documents_bucket_name
  dynamodb_table     = module.dynamodb.table_name

  tags = local.common_tags
}

################################################################################
# CloudFront Module (Conditional)
################################################################################

module "cloudfront" {
  count  = var.enable_cloudfront ? 1 : 0
  source = "./modules/cloudfront"

  project_name                = var.project_name
  environment                 = var.environment
  frontend_bucket_domain_name = module.s3.frontend_bucket_regional_domain_name
  frontend_bucket_id          = module.s3.frontend_bucket_id
  alb_dns_name                = module.alb.alb_dns_name
  price_class                 = var.cloudfront_price_class
  domain_name                 = var.domain_name

  tags = local.common_tags
}

################################################################################
# CloudWatch Module
################################################################################

module "cloudwatch" {
  source = "./modules/cloudwatch"

  project_name        = var.project_name
  environment         = var.environment
  aws_region          = var.aws_region
  ecs_cluster_name    = module.ecs.cluster_name
  ecs_service_name    = module.ecs.service_name
  rds_instance_id     = module.rds.db_instance_id
  alb_arn_suffix      = module.alb.alb_arn_suffix
  target_group_arn_suffix = module.alb.target_group_arn_suffix
  alarm_email         = var.alarm_email

  tags = local.common_tags
}

################################################################################
# CodePipeline Module (Conditional)
################################################################################

module "codepipeline" {
  count  = var.enable_codepipeline ? 1 : 0
  source = "./modules/codepipeline"

  project_name      = var.project_name
  environment       = var.environment
  aws_region        = var.aws_region
  github_repository = var.github_repository
  github_branch     = var.github_branch
  ecs_cluster_name  = module.ecs.cluster_name
  ecs_service_name  = module.ecs.service_name

  tags = local.common_tags
}
