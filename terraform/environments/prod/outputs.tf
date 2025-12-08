################################################################################
# Production Environment Outputs
################################################################################

output "vpc_id" {
  description = "VPC ID"
  value       = module.infrastructure.vpc_id
}

output "api_url" {
  description = "Backend API URL"
  value       = module.infrastructure.api_url
}

output "frontend_url" {
  description = "Frontend URL"
  value       = module.infrastructure.frontend_url
}

output "rds_endpoint" {
  description = "RDS endpoint"
  value       = module.infrastructure.rds_endpoint
  sensitive   = true
}

output "s3_bucket" {
  description = "Documents S3 bucket"
  value       = module.infrastructure.s3_documents_bucket_name
}

output "dynamodb_table" {
  description = "DynamoDB table"
  value       = module.infrastructure.dynamodb_table_name
}

output "ecs_cluster" {
  description = "ECS cluster name"
  value       = module.infrastructure.ecs_cluster_name
}

output "cloudfront_distribution_id" {
  description = "CloudFront distribution ID"
  value       = module.infrastructure.cloudfront_distribution_id
}

output "cloudfront_domain" {
  description = "CloudFront domain name"
  value       = module.infrastructure.cloudfront_domain_name
}

output "connection_info" {
  description = "Connection information for the application"
  value       = module.infrastructure.connection_info
  sensitive   = true
}
