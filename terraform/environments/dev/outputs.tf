output "vpc_id" {
  description = "VPC ID"
  value       = module.vpc.vpc_id
}

output "alb_dns_name" {
  description = "ALB DNS name - Use this to access the API"
  value       = module.alb.alb_dns_name
}

output "cloudfront_domain_name" {
  description = "CloudFront domain name - Use this to access the application"
  value       = module.cloudfront.distribution_domain_name
}

output "ecr_repository_url" {
  description = "ECR repository URL - Push Docker images here"
  value       = module.ecr.repository_url
}

output "s3_documents_bucket" {
  description = "S3 bucket for document storage"
  value       = module.s3.documents_bucket_name
}

output "s3_frontend_bucket" {
  description = "S3 bucket for frontend files"
  value       = module.s3.frontend_bucket_name
}

output "dynamodb_table_name" {
  description = "DynamoDB table name"
  value       = module.dynamodb.table_name
}

output "ecs_cluster_name" {
  description = "ECS cluster name"
  value       = module.ecs.cluster_name
}

output "ecs_service_name" {
  description = "ECS service name"
  value       = module.ecs.service_name
}

output "rds_endpoint" {
  description = "RDS endpoint"
  value       = module.rds.endpoint
  sensitive   = true
}
