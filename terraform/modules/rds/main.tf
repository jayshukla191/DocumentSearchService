################################################################################
# RDS PostgreSQL Module
# Creates RDS PostgreSQL instance with pgvector extension support
################################################################################

locals {
  name_prefix = "${var.project_name}-${var.environment}"
}

################################################################################
# Random Password Generation
################################################################################

resource "random_password" "master_password" {
  length           = 32
  special          = true
  override_special = "!#$%^&*()-_=+[]{}|:,.<>?"
}

################################################################################
# Secrets Manager for DB Password
################################################################################

resource "aws_secretsmanager_secret" "db_password" {
  name                    = "${local.name_prefix}-db-password"
  description             = "Master password for RDS PostgreSQL"
  recovery_window_in_days = var.environment == "prod" ? 7 : 0

  tags = var.tags
}

resource "aws_secretsmanager_secret_version" "db_password" {
  secret_id = aws_secretsmanager_secret.db_password.id
  secret_string = jsonencode({
    username = var.master_username
    password = random_password.master_password.result
    host     = aws_db_instance.main.address
    port     = aws_db_instance.main.port
    dbname   = var.database_name
  })
}

################################################################################
# DB Subnet Group
################################################################################

resource "aws_db_subnet_group" "main" {
  name        = "${local.name_prefix}-db-subnet-group"
  description = "Database subnet group for ${local.name_prefix}"
  subnet_ids  = var.subnet_ids

  tags = merge(var.tags, {
    Name = "${local.name_prefix}-db-subnet-group"
  })
}

################################################################################
# DB Parameter Group with pgvector support
################################################################################

resource "aws_db_parameter_group" "main" {
  name        = "${local.name_prefix}-pg-params"
  family      = "postgres14"
  description = "PostgreSQL parameter group with pgvector support"

  # Shared preload libraries for pgvector
  parameter {
    name         = "shared_preload_libraries"
    value        = "pg_stat_statements"
    apply_method = "pending-reboot"
  }

  # Performance tuning parameters
  parameter {
    name  = "log_min_duration_statement"
    value = "1000"  # Log queries taking more than 1 second
  }

  parameter {
    name  = "log_connections"
    value = "1"
  }

  parameter {
    name  = "log_disconnections"
    value = "1"
  }

  tags = merge(var.tags, {
    Name = "${local.name_prefix}-pg-params"
  })
}

################################################################################
# IAM Role for Enhanced Monitoring (Optional)
################################################################################

resource "aws_iam_role" "rds_monitoring" {
  count = var.enable_enhanced_monitoring ? 1 : 0

  name = "${local.name_prefix}-rds-monitoring-role"

  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Action = "sts:AssumeRole"
        Effect = "Allow"
        Principal = {
          Service = "monitoring.rds.amazonaws.com"
        }
      }
    ]
  })

  tags = var.tags
}

resource "aws_iam_role_policy_attachment" "rds_monitoring" {
  count = var.enable_enhanced_monitoring ? 1 : 0

  role       = aws_iam_role.rds_monitoring[0].name
  policy_arn = "arn:aws:iam::aws:policy/service-role/AmazonRDSEnhancedMonitoringRole"
}

################################################################################
# RDS PostgreSQL Instance
################################################################################

resource "aws_db_instance" "main" {
  identifier = "${local.name_prefix}-postgres"

  # Engine configuration
  engine               = "postgres"
  engine_version       = "14.10"
  instance_class       = var.instance_class
  parameter_group_name = aws_db_parameter_group.main.name

  # Storage configuration
  allocated_storage     = var.allocated_storage
  max_allocated_storage = var.max_allocated_storage
  storage_type          = "gp3"
  storage_encrypted     = true
  iops                  = var.environment == "prod" ? 3000 : null
  storage_throughput    = var.environment == "prod" ? 125 : null

  # Database configuration
  db_name  = var.database_name
  username = var.master_username
  password = random_password.master_password.result
  port     = 5432

  # Network configuration
  db_subnet_group_name   = aws_db_subnet_group.main.name
  vpc_security_group_ids = [var.security_group_id]
  publicly_accessible    = false
  multi_az               = var.multi_az

  # Backup configuration
  backup_retention_period = var.backup_retention_period
  backup_window           = "03:00-04:00"
  maintenance_window      = "Mon:04:00-Mon:05:00"
  copy_tags_to_snapshot   = true

  # Performance Insights
  performance_insights_enabled          = var.environment == "prod" ? true : false
  performance_insights_retention_period = var.environment == "prod" ? 7 : null

  # Enhanced Monitoring
  monitoring_interval = var.enable_enhanced_monitoring ? 60 : 0
  monitoring_role_arn = var.enable_enhanced_monitoring ? aws_iam_role.rds_monitoring[0].arn : null

  # Protection
  deletion_protection      = var.deletion_protection
  skip_final_snapshot      = var.environment != "prod"
  final_snapshot_identifier = var.environment == "prod" ? "${local.name_prefix}-final-snapshot" : null

  # Auto minor version upgrade
  auto_minor_version_upgrade = true
  apply_immediately          = var.environment != "prod"

  tags = merge(var.tags, {
    Name = "${local.name_prefix}-postgres"
  })

  lifecycle {
    prevent_destroy = false
  }
}

################################################################################
# CloudWatch Alarms for RDS
################################################################################

resource "aws_cloudwatch_metric_alarm" "db_cpu" {
  count = var.environment == "prod" ? 1 : 0

  alarm_name          = "${local.name_prefix}-rds-high-cpu"
  comparison_operator = "GreaterThanThreshold"
  evaluation_periods  = 2
  metric_name         = "CPUUtilization"
  namespace           = "AWS/RDS"
  period              = 300
  statistic           = "Average"
  threshold           = 80
  alarm_description   = "RDS CPU utilization is high"

  dimensions = {
    DBInstanceIdentifier = aws_db_instance.main.identifier
  }

  tags = var.tags
}

resource "aws_cloudwatch_metric_alarm" "db_storage" {
  count = var.environment == "prod" ? 1 : 0

  alarm_name          = "${local.name_prefix}-rds-low-storage"
  comparison_operator = "LessThanThreshold"
  evaluation_periods  = 1
  metric_name         = "FreeStorageSpace"
  namespace           = "AWS/RDS"
  period              = 300
  statistic           = "Average"
  threshold           = 5368709120  # 5 GB in bytes
  alarm_description   = "RDS free storage space is low"

  dimensions = {
    DBInstanceIdentifier = aws_db_instance.main.identifier
  }

  tags = var.tags
}

resource "aws_cloudwatch_metric_alarm" "db_connections" {
  count = var.environment == "prod" ? 1 : 0

  alarm_name          = "${local.name_prefix}-rds-high-connections"
  comparison_operator = "GreaterThanThreshold"
  evaluation_periods  = 2
  metric_name         = "DatabaseConnections"
  namespace           = "AWS/RDS"
  period              = 300
  statistic           = "Average"
  threshold           = 80
  alarm_description   = "RDS database connections are high"

  dimensions = {
    DBInstanceIdentifier = aws_db_instance.main.identifier
  }

  tags = var.tags
}
