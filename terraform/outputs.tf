################################################################################
# Outputs
################################################################################

# VPC Outputs
output "vpc_id" {
  description = "ID of the VPC"
  value       = module.vpc.vpc_id
}

output "public_subnet_ids" {
  description = "IDs of public subnets"
  value       = module.vpc.public_subnet_ids
}

output "private_subnet_ids" {
  description = "IDs of private subnets"
  value       = module.vpc.private_subnet_ids
}

# RDS Outputs
output "rds_endpoint" {
  description = "RDS instance endpoint"
  value       = module.rds.db_endpoint
}

output "rds_port" {
  description = "RDS instance port"
  value       = module.rds.db_port
}

output "rds_database_name" {
  description = "Name of the database"
  value       = module.rds.db_name
}

# S3 Outputs
output "s3_documents_bucket_name" {
  description = "Name of the documents S3 bucket"
  value       = module.s3.documents_bucket_name
}

output "s3_documents_bucket_arn" {
  description = "ARN of the documents S3 bucket"
  value       = module.s3.documents_bucket_arn
}

output "s3_frontend_bucket_name" {
  description = "Name of the frontend S3 bucket"
  value       = module.s3.frontend_bucket_name
}

output "s3_frontend_bucket_website_endpoint" {
  description = "Website endpoint for frontend S3 bucket"
  value       = module.s3.frontend_bucket_website_endpoint
}

# DynamoDB Outputs
output "dynamodb_table_name" {
  description = "Name of the DynamoDB table"
  value       = module.dynamodb.table_name
}

output "dynamodb_table_arn" {
  description = "ARN of the DynamoDB table"
  value       = module.dynamodb.table_arn
}

# ALB Outputs
output "alb_dns_name" {
  description = "DNS name of the Application Load Balancer"
  value       = module.alb.alb_dns_name
}

output "alb_zone_id" {
  description = "Zone ID of the Application Load Balancer"
  value       = module.alb.alb_zone_id
}

output "alb_arn" {
  description = "ARN of the Application Load Balancer"
  value       = module.alb.alb_arn
}

# ECS Outputs
output "ecs_cluster_name" {
  description = "Name of the ECS cluster"
  value       = module.ecs.cluster_name
}

output "ecs_cluster_arn" {
  description = "ARN of the ECS cluster"
  value       = module.ecs.cluster_arn
}

output "ecs_service_name" {
  description = "Name of the ECS service"
  value       = module.ecs.service_name
}

# CloudFront Outputs
output "cloudfront_distribution_id" {
  description = "ID of the CloudFront distribution"
  value       = var.enable_cloudfront ? module.cloudfront[0].distribution_id : null
}

output "cloudfront_domain_name" {
  description = "Domain name of the CloudFront distribution"
  value       = var.enable_cloudfront ? module.cloudfront[0].domain_name : null
}

# IAM Outputs
output "ecs_task_role_arn" {
  description = "ARN of the ECS task role"
  value       = module.iam.ecs_task_role_arn
}

output "ecs_execution_role_arn" {
  description = "ARN of the ECS execution role"
  value       = module.iam.ecs_execution_role_arn
}

# Application URLs
output "api_url" {
  description = "URL for the backend API"
  value       = "http://${module.alb.alb_dns_name}"
}

output "frontend_url" {
  description = "URL for the frontend (via CloudFront if enabled)"
  value       = var.enable_cloudfront ? "https://${module.cloudfront[0].domain_name}" : module.s3.frontend_bucket_website_endpoint
}

# Connection Information
output "connection_info" {
  description = "Connection information for the application"
  value = {
    api_endpoint     = "http://${module.alb.alb_dns_name}"
    database_host    = module.rds.db_endpoint
    database_port    = module.rds.db_port
    database_name    = module.rds.db_name
    s3_bucket        = module.s3.documents_bucket_name
    dynamodb_table   = module.dynamodb.table_name
    ecs_cluster      = module.ecs.cluster_name
    aws_region       = var.aws_region
  }
}
