variable "name_prefix" {
  description = "Prefix for resource names"
  type        = string
}

variable "frontend_bucket_id" {
  description = "S3 bucket ID for frontend"
  type        = string
}

variable "frontend_bucket_arn" {
  description = "S3 bucket ARN for frontend"
  type        = string
}

variable "frontend_bucket_domain" {
  description = "S3 bucket regional domain name"
  type        = string
}

variable "alb_dns_name" {
  description = "ALB DNS name"
  type        = string
}

variable "tags" {
  description = "Tags to apply to resources"
  type        = map(string)
  default     = {}
}


