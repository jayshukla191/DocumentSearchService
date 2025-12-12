# Primary provider for ap-south-1 (Mumbai)
provider "aws" {
  region = var.aws_region

  default_tags {
    tags = {
      Project     = "document-service"
      Environment = var.environment
      ManagedBy   = "terraform"
    }
  }
}

# Provider for us-east-1 (for Bedrock and ACM certificates for CloudFront)
provider "aws" {
  alias  = "us_east_1"
  region = "us-east-1"

  default_tags {
    tags = {
      Project     = "document-service"
      Environment = var.environment
      ManagedBy   = "terraform"
    }
  }
}


