################################################################################
# CloudWatch Module Outputs
################################################################################

output "dashboard_name" {
  description = "Name of the CloudWatch dashboard"
  value       = aws_cloudwatch_dashboard.main.dashboard_name
}

output "dashboard_arn" {
  description = "ARN of the CloudWatch dashboard"
  value       = aws_cloudwatch_dashboard.main.dashboard_arn
}

output "sns_topic_arn" {
  description = "ARN of the SNS topic for alarms"
  value       = aws_sns_topic.alarms.arn
}

output "sns_topic_name" {
  description = "Name of the SNS topic for alarms"
  value       = aws_sns_topic.alarms.name
}

output "alarm_arns" {
  description = "ARNs of CloudWatch alarms"
  value = {
    ecs_cpu_high        = aws_cloudwatch_metric_alarm.ecs_cpu_high.arn
    ecs_memory_high     = aws_cloudwatch_metric_alarm.ecs_memory_high.arn
    rds_cpu_high        = aws_cloudwatch_metric_alarm.rds_cpu_high.arn
    rds_storage_low     = aws_cloudwatch_metric_alarm.rds_storage_low.arn
    rds_connections_high = aws_cloudwatch_metric_alarm.rds_connections_high.arn
    alb_5xx_errors      = aws_cloudwatch_metric_alarm.alb_5xx_errors.arn
    alb_response_time   = aws_cloudwatch_metric_alarm.alb_response_time.arn
    unhealthy_hosts     = aws_cloudwatch_metric_alarm.unhealthy_hosts.arn
    application_errors  = aws_cloudwatch_metric_alarm.application_errors.arn
  }
}
