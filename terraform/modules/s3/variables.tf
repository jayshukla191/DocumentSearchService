################################################################################
# S3 Module Variables
################################################################################

variable "project_name" {
  description = "Name of the project"
  type        = string
}

variable "environment" {
  description = "Environment name"
  type        = string
}

variable "versioning_enabled" {
  description = "Enable versioning for documents bucket"
  type        = bool
  default     = true
}

variable "lifecycle_glacier_days" {
  description = "Days before transitioning objects to Glacier"
  type        = number
  default     = 90
}

variable "cloudfront_distribution_arn" {
  description = "ARN of the CloudFront distribution for bucket policy"
  type        = string
  default     = null
}

variable "tags" {
  description = "Tags to apply to resources"
  type        = map(string)
  default     = {}
}
