################################################################################
# S3 Module Outputs
################################################################################

# Documents Bucket
output "documents_bucket_name" {
  description = "Name of the documents S3 bucket"
  value       = aws_s3_bucket.documents.id
}

output "documents_bucket_arn" {
  description = "ARN of the documents S3 bucket"
  value       = aws_s3_bucket.documents.arn
}

output "documents_bucket_domain_name" {
  description = "Domain name of the documents S3 bucket"
  value       = aws_s3_bucket.documents.bucket_domain_name
}

output "documents_bucket_regional_domain_name" {
  description = "Regional domain name of the documents S3 bucket"
  value       = aws_s3_bucket.documents.bucket_regional_domain_name
}

# Frontend Bucket
output "frontend_bucket_name" {
  description = "Name of the frontend S3 bucket"
  value       = aws_s3_bucket.frontend.id
}

output "frontend_bucket_id" {
  description = "ID of the frontend S3 bucket"
  value       = aws_s3_bucket.frontend.id
}

output "frontend_bucket_arn" {
  description = "ARN of the frontend S3 bucket"
  value       = aws_s3_bucket.frontend.arn
}

output "frontend_bucket_domain_name" {
  description = "Domain name of the frontend S3 bucket"
  value       = aws_s3_bucket.frontend.bucket_domain_name
}

output "frontend_bucket_regional_domain_name" {
  description = "Regional domain name of the frontend S3 bucket"
  value       = aws_s3_bucket.frontend.bucket_regional_domain_name
}

output "frontend_bucket_website_endpoint" {
  description = "Website endpoint for the frontend S3 bucket"
  value       = aws_s3_bucket_website_configuration.frontend.website_endpoint
}

output "frontend_bucket_website_domain" {
  description = "Website domain for the frontend S3 bucket"
  value       = aws_s3_bucket_website_configuration.frontend.website_domain
}

# CloudFront OAC
output "cloudfront_oac_id" {
  description = "ID of the CloudFront Origin Access Control"
  value       = aws_cloudfront_origin_access_control.frontend.id
}

# Logs Bucket (if exists)
output "logs_bucket_name" {
  description = "Name of the logs S3 bucket"
  value       = var.environment == "prod" ? aws_s3_bucket.logs[0].id : null
}
