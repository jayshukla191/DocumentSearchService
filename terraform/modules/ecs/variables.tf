################################################################################
# ECS Module Variables
################################################################################

variable "project_name" {
  description = "Name of the project"
  type        = string
}

variable "environment" {
  description = "Environment name"
  type        = string
}

variable "aws_region" {
  description = "AWS region"
  type        = string
}

variable "vpc_id" {
  description = "ID of the VPC"
  type        = string
}

variable "private_subnet_ids" {
  description = "List of private subnet IDs"
  type        = list(string)
}

variable "security_group_id" {
  description = "Security group ID for ECS tasks"
  type        = string
}

variable "task_cpu" {
  description = "CPU units for ECS task"
  type        = number
  default     = 512
}

variable "task_memory" {
  description = "Memory for ECS task in MB"
  type        = number
  default     = 1024
}

variable "container_port" {
  description = "Port the container listens on"
  type        = number
  default     = 8080
}

variable "backend_image" {
  description = "Docker image for backend service"
  type        = string
  default     = ""
}

variable "desired_count" {
  description = "Desired number of ECS tasks"
  type        = number
  default     = 2
}

variable "min_capacity" {
  description = "Minimum number of ECS tasks"
  type        = number
  default     = 1
}

variable "max_capacity" {
  description = "Maximum number of ECS tasks"
  type        = number
  default     = 4
}

variable "execution_role_arn" {
  description = "ARN of the ECS execution role"
  type        = string
}

variable "task_role_arn" {
  description = "ARN of the ECS task role"
  type        = string
}

variable "target_group_arn" {
  description = "ARN of the ALB target group"
  type        = string
}

variable "db_host" {
  description = "Database host"
  type        = string
}

variable "db_port" {
  description = "Database port"
  type        = number
}

variable "db_name" {
  description = "Database name"
  type        = string
}

variable "db_username" {
  description = "Database username"
  type        = string
  sensitive   = true
}

variable "db_password_secret_arn" {
  description = "ARN of the Secrets Manager secret containing DB password"
  type        = string
}

variable "s3_bucket_name" {
  description = "Name of the S3 bucket for documents"
  type        = string
}

variable "dynamodb_table" {
  description = "Name of the DynamoDB table"
  type        = string
}

variable "tags" {
  description = "Tags to apply to resources"
  type        = map(string)
  default     = {}
}
