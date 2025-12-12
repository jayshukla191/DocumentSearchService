# Terraform state backend configuration
# Note: You need to create this S3 bucket and DynamoDB table manually first,
# or use the bootstrap script provided in terraform/scripts/bootstrap.sh

terraform {
  backend "s3" {
    bucket         = "document-service-terraform-state"
    key            = "state/terraform.tfstate"
    region         = "ap-south-1"
    encrypt        = true
    dynamodb_table = "document-service-terraform-locks"
  }
}


