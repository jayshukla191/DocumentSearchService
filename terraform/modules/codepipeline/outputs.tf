################################################################################
# CodePipeline Module Outputs
################################################################################

output "pipeline_name" {
  description = "Name of the CodePipeline"
  value       = aws_codepipeline.main.name
}

output "pipeline_arn" {
  description = "ARN of the CodePipeline"
  value       = aws_codepipeline.main.arn
}

output "codebuild_project_name" {
  description = "Name of the CodeBuild project"
  value       = aws_codebuild_project.backend.name
}

output "codebuild_project_arn" {
  description = "ARN of the CodeBuild project"
  value       = aws_codebuild_project.backend.arn
}

output "artifacts_bucket_name" {
  description = "Name of the S3 bucket for artifacts"
  value       = aws_s3_bucket.artifacts.bucket
}

output "artifacts_bucket_arn" {
  description = "ARN of the S3 bucket for artifacts"
  value       = aws_s3_bucket.artifacts.arn
}

output "github_connection_arn" {
  description = "ARN of the GitHub CodeStar connection"
  value       = aws_codestarconnections_connection.github.arn
}

output "github_connection_status" {
  description = "Status of the GitHub CodeStar connection"
  value       = aws_codestarconnections_connection.github.connection_status
}

output "sns_topic_arn" {
  description = "ARN of the SNS topic for pipeline notifications"
  value       = aws_sns_topic.pipeline.arn
}
