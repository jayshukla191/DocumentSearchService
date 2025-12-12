output "vpc_id" {
  description = "VPC ID"
  value       = module.document_service.vpc_id
}

output "alb_dns_name" {
  description = "ALB DNS name - Use this to access the API"
  value       = module.document_service.alb_dns_name
}

output "cloudfront_domain_name" {
  description = "CloudFront domain name - Use this to access the application"
  value       = module.document_service.cloudfront_domain_name
}

output "ecr_repository_url" {
  description = "ECR repository URL - Push Docker images here"
  value       = module.document_service.ecr_repository_url
}

output "s3_documents_bucket" {
  description = "S3 bucket for document storage"
  value       = module.document_service.s3_documents_bucket
}

output "s3_frontend_bucket" {
  description = "S3 bucket for frontend files"
  value       = module.document_service.s3_frontend_bucket
}

output "dynamodb_table_name" {
  description = "DynamoDB table name"
  value       = module.document_service.dynamodb_table_name
}

output "ecs_cluster_name" {
  description = "ECS cluster name"
  value       = module.document_service.ecs_cluster_name
}

output "ecs_service_name" {
  description = "ECS service name"
  value       = module.document_service.ecs_service_name
}


