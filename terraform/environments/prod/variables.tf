################################################################################
# Production Environment Variables
################################################################################

variable "project_name" {
  description = "Name of the project"
  type        = string
  default     = "document-service"
}

variable "environment" {
  description = "Environment name"
  type        = string
  default     = "prod"
}

variable "aws_region" {
  description = "AWS region"
  type        = string
  default     = "us-east-1"
}

variable "tags" {
  description = "Additional tags"
  type        = map(string)
  default     = {}
}

# VPC
variable "vpc_cidr" {
  description = "VPC CIDR block"
  type        = string
  default     = "10.0.0.0/16"
}

variable "availability_zones" {
  description = "Availability zones"
  type        = list(string)
  default     = ["us-east-1a", "us-east-1b"]
}

variable "public_subnet_cidrs" {
  description = "Public subnet CIDR blocks"
  type        = list(string)
  default     = ["10.0.1.0/24", "10.0.2.0/24"]
}

variable "private_subnet_cidrs" {
  description = "Private subnet CIDR blocks"
  type        = list(string)
  default     = ["10.0.10.0/24", "10.0.11.0/24"]
}

variable "enable_nat_gateway" {
  description = "Enable NAT Gateway"
  type        = bool
  default     = true
}

variable "single_nat_gateway" {
  description = "Use single NAT Gateway"
  type        = bool
  default     = false  # Use multiple NAT Gateways for HA in prod
}

# RDS
variable "db_instance_class" {
  description = "RDS instance class"
  type        = string
  default     = "db.r6g.large"
}

variable "db_allocated_storage" {
  description = "RDS allocated storage in GB"
  type        = number
  default     = 100
}

variable "db_max_allocated_storage" {
  description = "RDS max allocated storage in GB"
  type        = number
  default     = 500
}

variable "db_name" {
  description = "Database name"
  type        = string
  default     = "document_management"
}

variable "db_username" {
  description = "Database username"
  type        = string
  default     = "docadmin"
  sensitive   = true
}

variable "db_multi_az" {
  description = "Enable Multi-AZ"
  type        = bool
  default     = true
}

variable "db_backup_retention_period" {
  description = "Backup retention period"
  type        = number
  default     = 30
}

variable "db_deletion_protection" {
  description = "Enable deletion protection"
  type        = bool
  default     = true
}

# ECS
variable "ecs_task_cpu" {
  description = "ECS task CPU units"
  type        = number
  default     = 1024
}

variable "ecs_task_memory" {
  description = "ECS task memory in MB"
  type        = number
  default     = 2048
}

variable "ecs_desired_count" {
  description = "ECS desired task count"
  type        = number
  default     = 3
}

variable "ecs_min_capacity" {
  description = "ECS minimum task count"
  type        = number
  default     = 2
}

variable "ecs_max_capacity" {
  description = "ECS maximum task count"
  type        = number
  default     = 10
}

variable "container_port" {
  description = "Container port"
  type        = number
  default     = 8080
}

variable "health_check_path" {
  description = "Health check path"
  type        = string
  default     = "/actuator/health"
}

variable "backend_image" {
  description = "Backend Docker image"
  type        = string
  default     = ""
}

# S3
variable "s3_versioning_enabled" {
  description = "Enable S3 versioning"
  type        = bool
  default     = true
}

variable "s3_lifecycle_glacier_days" {
  description = "Days before Glacier transition"
  type        = number
  default     = 90
}

# DynamoDB
variable "dynamodb_billing_mode" {
  description = "DynamoDB billing mode"
  type        = string
  default     = "PROVISIONED"
}

variable "dynamodb_read_capacity" {
  description = "DynamoDB read capacity"
  type        = number
  default     = 100
}

variable "dynamodb_write_capacity" {
  description = "DynamoDB write capacity"
  type        = number
  default     = 100
}

# CloudFront
variable "enable_cloudfront" {
  description = "Enable CloudFront"
  type        = bool
  default     = true
}

variable "cloudfront_price_class" {
  description = "CloudFront price class"
  type        = string
  default     = "PriceClass_All"
}

# Monitoring
variable "alarm_email" {
  description = "Alarm notification email"
  type        = string
  default     = ""
}

variable "enable_enhanced_monitoring" {
  description = "Enable enhanced monitoring"
  type        = bool
  default     = true
}

# CI/CD
variable "enable_codepipeline" {
  description = "Enable CodePipeline"
  type        = bool
  default     = true
}

variable "github_repository" {
  description = "GitHub repository"
  type        = string
  default     = ""
}

variable "github_branch" {
  description = "GitHub branch"
  type        = string
  default     = "main"
}

# Domain
variable "domain_name" {
  description = "Domain name"
  type        = string
  default     = ""
}

variable "create_route53_records" {
  description = "Create Route 53 records"
  type        = bool
  default     = false
}
