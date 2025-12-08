################################################################################
# IAM Module Outputs
################################################################################

output "ecs_execution_role_arn" {
  description = "ARN of the ECS execution role"
  value       = aws_iam_role.ecs_execution.arn
}

output "ecs_execution_role_name" {
  description = "Name of the ECS execution role"
  value       = aws_iam_role.ecs_execution.name
}

output "ecs_task_role_arn" {
  description = "ARN of the ECS task role"
  value       = aws_iam_role.ecs_task.arn
}

output "ecs_task_role_name" {
  description = "Name of the ECS task role"
  value       = aws_iam_role.ecs_task.name
}

output "codepipeline_role_arn" {
  description = "ARN of the CodePipeline role"
  value       = aws_iam_role.codepipeline.arn
}

output "codepipeline_role_name" {
  description = "Name of the CodePipeline role"
  value       = aws_iam_role.codepipeline.name
}

output "codebuild_role_arn" {
  description = "ARN of the CodeBuild role"
  value       = aws_iam_role.codebuild.arn
}

output "codebuild_role_name" {
  description = "Name of the CodeBuild role"
  value       = aws_iam_role.codebuild.name
}
