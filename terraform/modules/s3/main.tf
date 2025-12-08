################################################################################
# S3 Module
# Creates S3 buckets for document storage and frontend hosting
################################################################################

locals {
  name_prefix = "${var.project_name}-${var.environment}"
}

################################################################################
# Random Suffix for Bucket Names
################################################################################

resource "random_id" "bucket_suffix" {
  byte_length = 4
}

################################################################################
# Documents Storage Bucket
################################################################################

resource "aws_s3_bucket" "documents" {
  bucket        = "${local.name_prefix}-documents-${random_id.bucket_suffix.hex}"
  force_destroy = var.environment != "prod"

  tags = merge(var.tags, {
    Name    = "${local.name_prefix}-documents"
    Purpose = "Document storage"
  })
}

resource "aws_s3_bucket_versioning" "documents" {
  bucket = aws_s3_bucket.documents.id

  versioning_configuration {
    status = var.versioning_enabled ? "Enabled" : "Suspended"
  }
}

resource "aws_s3_bucket_server_side_encryption_configuration" "documents" {
  bucket = aws_s3_bucket.documents.id

  rule {
    apply_server_side_encryption_by_default {
      sse_algorithm = "AES256"
    }
    bucket_key_enabled = true
  }
}

resource "aws_s3_bucket_public_access_block" "documents" {
  bucket = aws_s3_bucket.documents.id

  block_public_acls       = true
  block_public_policy     = true
  ignore_public_acls      = true
  restrict_public_buckets = true
}

resource "aws_s3_bucket_lifecycle_configuration" "documents" {
  bucket = aws_s3_bucket.documents.id

  rule {
    id     = "transition-to-glacier"
    status = var.environment == "prod" ? "Enabled" : "Disabled"

    transition {
      days          = var.lifecycle_glacier_days
      storage_class = "GLACIER"
    }
  }

  rule {
    id     = "cleanup-incomplete-multipart"
    status = "Enabled"

    abort_incomplete_multipart_upload {
      days_after_initiation = 7
    }
  }

  rule {
    id     = "cleanup-old-versions"
    status = var.versioning_enabled ? "Enabled" : "Disabled"

    noncurrent_version_expiration {
      noncurrent_days = 90
    }
  }
}

resource "aws_s3_bucket_cors_configuration" "documents" {
  bucket = aws_s3_bucket.documents.id

  cors_rule {
    allowed_headers = ["*"]
    allowed_methods = ["GET", "PUT", "POST", "DELETE", "HEAD"]
    allowed_origins = ["*"]  # Restrict in production
    expose_headers  = ["ETag"]
    max_age_seconds = 3000
  }
}

################################################################################
# Frontend Static Hosting Bucket
################################################################################

resource "aws_s3_bucket" "frontend" {
  bucket        = "${local.name_prefix}-frontend-${random_id.bucket_suffix.hex}"
  force_destroy = var.environment != "prod"

  tags = merge(var.tags, {
    Name    = "${local.name_prefix}-frontend"
    Purpose = "Frontend static hosting"
  })
}

resource "aws_s3_bucket_versioning" "frontend" {
  bucket = aws_s3_bucket.frontend.id

  versioning_configuration {
    status = "Enabled"
  }
}

resource "aws_s3_bucket_server_side_encryption_configuration" "frontend" {
  bucket = aws_s3_bucket.frontend.id

  rule {
    apply_server_side_encryption_by_default {
      sse_algorithm = "AES256"
    }
  }
}

resource "aws_s3_bucket_public_access_block" "frontend" {
  bucket = aws_s3_bucket.frontend.id

  block_public_acls       = true
  block_public_policy     = true
  ignore_public_acls      = true
  restrict_public_buckets = true
}

resource "aws_s3_bucket_website_configuration" "frontend" {
  bucket = aws_s3_bucket.frontend.id

  index_document {
    suffix = "index.html"
  }

  error_document {
    key = "index.html"  # SPA routing support
  }
}

################################################################################
# CloudFront OAC for Frontend Bucket
################################################################################

resource "aws_cloudfront_origin_access_control" "frontend" {
  name                              = "${local.name_prefix}-frontend-oac"
  description                       = "OAC for frontend S3 bucket"
  origin_access_control_origin_type = "s3"
  signing_behavior                  = "always"
  signing_protocol                  = "sigv4"
}

################################################################################
# S3 Bucket Policy for CloudFront Access
################################################################################

data "aws_caller_identity" "current" {}

resource "aws_s3_bucket_policy" "frontend" {
  count  = var.cloudfront_distribution_arn != null ? 1 : 0
  bucket = aws_s3_bucket.frontend.id

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Sid    = "AllowCloudFrontServicePrincipal"
        Effect = "Allow"
        Principal = {
          Service = "cloudfront.amazonaws.com"
        }
        Action   = "s3:GetObject"
        Resource = "${aws_s3_bucket.frontend.arn}/*"
        Condition = {
          StringEquals = {
            "AWS:SourceArn" = var.cloudfront_distribution_arn
          }
        }
      }
    ]
  })
}

################################################################################
# S3 Access Logs Bucket (Optional for Production)
################################################################################

resource "aws_s3_bucket" "logs" {
  count = var.environment == "prod" ? 1 : 0

  bucket        = "${local.name_prefix}-logs-${random_id.bucket_suffix.hex}"
  force_destroy = false

  tags = merge(var.tags, {
    Name    = "${local.name_prefix}-logs"
    Purpose = "S3 access logs"
  })
}

resource "aws_s3_bucket_lifecycle_configuration" "logs" {
  count  = var.environment == "prod" ? 1 : 0
  bucket = aws_s3_bucket.logs[0].id

  rule {
    id     = "expire-logs"
    status = "Enabled"

    expiration {
      days = 90
    }
  }
}

resource "aws_s3_bucket_public_access_block" "logs" {
  count  = var.environment == "prod" ? 1 : 0
  bucket = aws_s3_bucket.logs[0].id

  block_public_acls       = true
  block_public_policy     = true
  ignore_public_acls      = true
  restrict_public_buckets = true
}
