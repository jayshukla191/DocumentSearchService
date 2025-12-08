################################################################################
# DynamoDB Module Outputs
################################################################################

output "table_name" {
  description = "Name of the DynamoDB table"
  value       = aws_dynamodb_table.document_metadata.name
}

output "table_arn" {
  description = "ARN of the DynamoDB table"
  value       = aws_dynamodb_table.document_metadata.arn
}

output "table_id" {
  description = "ID of the DynamoDB table"
  value       = aws_dynamodb_table.document_metadata.id
}

output "table_stream_arn" {
  description = "Stream ARN of the DynamoDB table"
  value       = aws_dynamodb_table.document_metadata.stream_arn
}

output "gsi_user_id_arn" {
  description = "ARN of the UserId GSI"
  value       = "${aws_dynamodb_table.document_metadata.arn}/index/UserId-index"
}
