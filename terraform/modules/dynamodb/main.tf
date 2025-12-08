################################################################################
# DynamoDB Module
# Creates DynamoDB table for document metadata
################################################################################

locals {
  name_prefix = "${var.project_name}-${var.environment}"
}

################################################################################
# DynamoDB Table - DocumentMetadata
################################################################################

resource "aws_dynamodb_table" "document_metadata" {
  name         = "${local.name_prefix}-DocumentMetadata"
  billing_mode = var.billing_mode

  # Only set capacity when using PROVISIONED mode
  read_capacity  = var.billing_mode == "PROVISIONED" ? var.read_capacity : null
  write_capacity = var.billing_mode == "PROVISIONED" ? var.write_capacity : null

  # Partition Key
  hash_key = "DocumentId"

  attribute {
    name = "DocumentId"
    type = "S"
  }

  # Global Secondary Index for user queries
  attribute {
    name = "UserId"
    type = "S"
  }

  global_secondary_index {
    name            = "UserId-index"
    hash_key        = "UserId"
    projection_type = "ALL"
    
    read_capacity  = var.billing_mode == "PROVISIONED" ? var.read_capacity : null
    write_capacity = var.billing_mode == "PROVISIONED" ? var.write_capacity : null
  }

  # Point-in-time Recovery
  point_in_time_recovery {
    enabled = var.environment == "prod"
  }

  # Server-side encryption
  server_side_encryption {
    enabled = true
  }

  # TTL (optional - for temporary documents)
  ttl {
    attribute_name = "ExpirationTime"
    enabled        = true
  }

  tags = merge(var.tags, {
    Name = "${local.name_prefix}-DocumentMetadata"
  })
}

################################################################################
# Auto Scaling for Provisioned Mode
################################################################################

resource "aws_appautoscaling_target" "dynamodb_read" {
  count = var.billing_mode == "PROVISIONED" && var.environment == "prod" ? 1 : 0

  max_capacity       = var.read_capacity * 10
  min_capacity       = var.read_capacity
  resource_id        = "table/${aws_dynamodb_table.document_metadata.name}"
  scalable_dimension = "dynamodb:table:ReadCapacityUnits"
  service_namespace  = "dynamodb"
}

resource "aws_appautoscaling_policy" "dynamodb_read" {
  count = var.billing_mode == "PROVISIONED" && var.environment == "prod" ? 1 : 0

  name               = "${local.name_prefix}-dynamodb-read-scaling"
  policy_type        = "TargetTrackingScaling"
  resource_id        = aws_appautoscaling_target.dynamodb_read[0].resource_id
  scalable_dimension = aws_appautoscaling_target.dynamodb_read[0].scalable_dimension
  service_namespace  = aws_appautoscaling_target.dynamodb_read[0].service_namespace

  target_tracking_scaling_policy_configuration {
    predefined_metric_specification {
      predefined_metric_type = "DynamoDBReadCapacityUtilization"
    }
    target_value = 70.0
  }
}

resource "aws_appautoscaling_target" "dynamodb_write" {
  count = var.billing_mode == "PROVISIONED" && var.environment == "prod" ? 1 : 0

  max_capacity       = var.write_capacity * 10
  min_capacity       = var.write_capacity
  resource_id        = "table/${aws_dynamodb_table.document_metadata.name}"
  scalable_dimension = "dynamodb:table:WriteCapacityUnits"
  service_namespace  = "dynamodb"
}

resource "aws_appautoscaling_policy" "dynamodb_write" {
  count = var.billing_mode == "PROVISIONED" && var.environment == "prod" ? 1 : 0

  name               = "${local.name_prefix}-dynamodb-write-scaling"
  policy_type        = "TargetTrackingScaling"
  resource_id        = aws_appautoscaling_target.dynamodb_write[0].resource_id
  scalable_dimension = aws_appautoscaling_target.dynamodb_write[0].scalable_dimension
  service_namespace  = aws_appautoscaling_target.dynamodb_write[0].service_namespace

  target_tracking_scaling_policy_configuration {
    predefined_metric_specification {
      predefined_metric_type = "DynamoDBWriteCapacityUtilization"
    }
    target_value = 70.0
  }
}

################################################################################
# CloudWatch Alarms
################################################################################

resource "aws_cloudwatch_metric_alarm" "dynamodb_throttled_requests" {
  count = var.environment == "prod" ? 1 : 0

  alarm_name          = "${local.name_prefix}-dynamodb-throttled-requests"
  comparison_operator = "GreaterThanThreshold"
  evaluation_periods  = 2
  metric_name         = "ThrottledRequests"
  namespace           = "AWS/DynamoDB"
  period              = 300
  statistic           = "Sum"
  threshold           = 10
  alarm_description   = "DynamoDB throttled requests detected"

  dimensions = {
    TableName = aws_dynamodb_table.document_metadata.name
  }

  tags = var.tags
}
