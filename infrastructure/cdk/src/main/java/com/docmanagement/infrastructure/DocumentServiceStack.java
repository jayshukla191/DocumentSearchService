package com.docmanagement.infrastructure;

import software.amazon.awscdk.*;
import software.amazon.awscdk.services.ec2.*;
import software.amazon.awscdk.services.ecs.*;
import software.amazon.awscdk.services.ecs.patterns.*;
import software.amazon.awscdk.services.rds.*;
import software.amazon.awscdk.services.s3.*;
import software.amazon.awscdk.services.dynamodb.*;
import software.amazon.awscdk.services.iam.*;
import software.amazon.awscdk.services.secretsmanager.*;
import software.amazon.awscdk.services.logs.*;
import software.amazon.awscdk.services.elasticloadbalancingv2.*;
import software.amazon.awscdk.services.certificatemanager.*;
import software.amazon.awscdk.services.route53.*;
import software.amazon.awscdk.services.cloudfront.*;
import software.amazon.awscdk.services.cloudfront.origins.*;
import software.constructs.Construct;

import java.util.*;

/**
 * Main CDK Stack for Document Management Service
 * 
 * This stack creates:
 * - VPC with public/private subnets
 * - RDS PostgreSQL with pgvector
 * - ECS Fargate cluster and service
 * - S3 buckets for documents and frontend
 * - DynamoDB table for metadata
 * - Application Load Balancer
 * - CloudFront distribution
 * - IAM roles and policies
 * - Secrets Manager for credentials
 */
public class DocumentServiceStack extends Stack {
    
    public DocumentServiceStack(final Construct scope, final String id, final StackProps props) {
        this(scope, id, props, null);
    }

    public DocumentServiceStack(final Construct scope, final String id, final StackProps props, 
                                final DocumentServiceStackProps stackProps) {
        super(scope, id, props);

        // Use provided props or defaults
        final String environment = stackProps != null ? stackProps.getEnvironment() : "dev";
        final String domainName = stackProps != null ? stackProps.getDomainName() : null;
        
        // ========== VPC ==========
        Vpc vpc = Vpc.Builder.create(this, "DocumentServiceVpc")
            .maxAzs(2)
            .natGateways(environment.equals("prod") ? 2 : 1) // Multi-AZ NAT for production
            .subnetConfiguration(Arrays.asList(
                SubnetConfiguration.builder()
                    .name("Public")
                    .subnetType(SubnetType.PUBLIC)
                    .cidrMask(24)
                    .build(),
                SubnetConfiguration.builder()
                    .name("Private")
                    .subnetType(SubnetType.PRIVATE_WITH_EGRESS)
                    .cidrMask(24)
                    .build()
            ))
            .build();

        // ========== Security Groups ==========
        SecurityGroup albSecurityGroup = SecurityGroup.Builder.create(this, "AlbSecurityGroup")
            .vpc(vpc)
            .description("Security group for Application Load Balancer")
            .allowAllOutbound(true)
            .build();
        
        albSecurityGroup.addIngressRule(
            Peer.anyIpv4(),
            Port.tcp(80),
            "Allow HTTP from internet"
        );
        albSecurityGroup.addIngressRule(
            Peer.anyIpv4(),
            Port.tcp(443),
            "Allow HTTPS from internet"
        );

        SecurityGroup ecsSecurityGroup = SecurityGroup.Builder.create(this, "EcsSecurityGroup")
            .vpc(vpc)
            .description("Security group for ECS tasks")
            .allowAllOutbound(true)
            .build();
        
        ecsSecurityGroup.addIngressRule(
            albSecurityGroup,
            Port.tcp(8080),
            "Allow traffic from ALB"
        );

        SecurityGroup rdsSecurityGroup = SecurityGroup.Builder.create(this, "RdsSecurityGroup")
            .vpc(vpc)
            .description("Security group for RDS PostgreSQL")
            .allowAllOutbound(false)
            .build();
        
        rdsSecurityGroup.addIngressRule(
            ecsSecurityGroup,
            Port.tcp(5432),
            "Allow PostgreSQL from ECS tasks"
        );

        // ========== Secrets Manager ==========
        // Database credentials secret
        Secret dbSecret = Secret.Builder.create(this, "DbSecret")
            .description("Database credentials for Document Service")
            .generateSecretString(SecretStringGenerator.builder()
                .secretStringTemplate("{\"username\":\"postgres\"}")
                .generateStringKey("password")
                .excludeCharacters("\"@/\\")
                .build())
            .build();

        // JWT secret
        Secret jwtSecret = Secret.Builder.create(this, "JwtSecret")
            .description("JWT secret key for Document Service")
            .generateSecretString(SecretStringGenerator.builder()
                .secretStringTemplate("{}")
                .generateStringKey("secret")
                .passwordLength(64)
                .excludeCharacters("\"@/\\")
                .build())
            .build();

        // ========== RDS PostgreSQL ==========
        DatabaseInstance dbInstance = DatabaseInstance.Builder.create(this, "DocumentDatabase")
            .engine(DatabaseInstanceEngine.postgres(
                PostgresInstanceEngineProps.builder()
                    .version(PostgresEngineVersion.VER_14_10)
                    .build()))
            .instanceType(InstanceType.of(
                InstanceClass.BURSTABLE3,
                environment.equals("prod") ? InstanceSize.LARGE : InstanceSize.MICRO))
            .vpc(vpc)
            .vpcSubnets(SubnetSelection.builder()
                .subnetType(SubnetType.PRIVATE_WITH_EGRESS)
                .build())
            .securityGroups(Arrays.asList(rdsSecurityGroup))
            .credentials(Credentials.fromSecret(dbSecret))
            .databaseName("document_management")
            .allocatedStorage(environment.equals("prod") ? 100 : 20)
            .storageType(StorageType.GP3)
            .multiAz(environment.equals("prod"))
            .backupRetention(Duration.days(environment.equals("prod") ? 7 : 1))
            .deleteAutomatedBackups(!environment.equals("prod"))
            .deletionProtection(environment.equals("prod"))
            .removalPolicy(environment.equals("prod") ? 
                RemovalPolicy.RETAIN : RemovalPolicy.DESTROY)
            .storageEncrypted(true)
            .build();

        // Add pgvector extension via parameter group
        ParameterGroup pgVectorParameterGroup = ParameterGroup.Builder.create(this, "PgVectorParameterGroup")
            .engine(DatabaseInstanceEngine.postgres(
                PostgresInstanceEngineProps.builder()
                    .version(PostgresEngineVersion.VER_14_10)
                    .build()))
            .description("Parameter group for pgvector extension")
            .build();

        // Note: pgvector extension needs to be installed manually after RDS creation
        // Run: CREATE EXTENSION vector; in the database

        // ========== S3 Buckets ==========
        Bucket documentsBucket = Bucket.Builder.create(this, "DocumentsBucket")
            .bucketName(String.format("document-service-files-%s-%s", 
                environment, getAccount()))
            .versioned(true)
            .encryption(BucketEncryption.S3_MANAGED)
            .blockPublicAccess(BlockPublicAccess.BLOCK_ALL)
            .enforceSSL(true)
            .removalPolicy(environment.equals("prod") ? 
                RemovalPolicy.RETAIN : RemovalPolicy.DESTROY)
            .autoDeleteObjects(!environment.equals("prod"))
            .lifecycleRules(Arrays.asList(
                LifecycleRule.builder()
                    .id("DeleteIncompleteMultipartUploads")
                    .abortIncompleteMultipartUploadAfter(Duration.days(7))
                    .build(),
                LifecycleRule.builder()
                    .id("MoveToGlacier")
                    .enabled(environment.equals("prod"))
                    .transitions(Arrays.asList(
                        Transition.builder()
                            .storageClass(StorageClass.GLACIER)
                            .transitionAfter(Duration.days(90))
                            .build()))
                    .build()
            ))
            .build();

        Bucket frontendBucket = Bucket.Builder.create(this, "FrontendBucket")
            .bucketName(String.format("document-service-frontend-%s-%s", 
                environment, getAccount()))
            .websiteIndexDocument("index.html")
            .websiteErrorDocument("index.html")
            .publicReadAccess(false)
            .blockPublicAccess(BlockPublicAccess.BLOCK_ALL)
            .encryption(BucketEncryption.S3_MANAGED)
            .removalPolicy(environment.equals("prod") ? 
                RemovalPolicy.RETAIN : RemovalPolicy.DESTROY)
            .autoDeleteObjects(!environment.equals("prod"))
            .build();

        // ========== DynamoDB ==========
        Table dynamoTable = Table.Builder.create(this, "DocumentMetadataTable")
            .tableName("DocumentMetadata")
            .partitionKey(Attribute.builder()
                .name("DocumentId")
                .type(AttributeType.STRING)
                .build())
            .billingMode(environment.equals("prod") ? 
                BillingMode.PROVISIONED : BillingMode.PAY_PER_REQUEST)
            .readCapacity(environment.equals("prod") ? 1000L : null)
            .writeCapacity(environment.equals("prod") ? 1000L : null)
            .encryption(TableEncryption.AWS_MANAGED)
            .pointInTimeRecovery(environment.equals("prod"))
            .removalPolicy(environment.equals("prod") ? 
                RemovalPolicy.RETAIN : RemovalPolicy.DESTROY)
            .build();

        // ========== IAM Roles ==========
        // ECS Task Execution Role
        Role ecsTaskExecutionRole = Role.Builder.create(this, "EcsTaskExecutionRole")
            .assumedBy(new ServicePrincipal("ecs-tasks.amazonaws.com"))
            .managedPolicies(Arrays.asList(
                ManagedPolicy.fromAwsManagedPolicyName("service-role/AmazonECSTaskExecutionRolePolicy")
            ))
            .build();

        dbSecret.grantRead(ecsTaskExecutionRole);
        jwtSecret.grantRead(ecsTaskExecutionRole);

        // ECS Task Role (for application permissions)
        Role ecsTaskRole = Role.Builder.create(this, "EcsTaskRole")
            .assumedBy(new ServicePrincipal("ecs-tasks.amazonaws.com"))
            .build();

        // Grant S3 permissions
        documentsBucket.grantReadWrite(ecsTaskRole);
        
        // Grant DynamoDB permissions
        dynamoTable.grantReadWriteData(ecsTaskRole);
        
        // Grant Bedrock permissions
        ecsTaskRole.addToPolicy(PolicyStatement.Builder.create()
            .effect(Effect.ALLOW)
            .actions(Arrays.asList(
                "bedrock:InvokeModel",
                "bedrock:InvokeModelWithResponseStream"
            ))
            .resources(Arrays.asList(
                String.format("arn:aws:bedrock:%s::foundation-model/amazon.titan-embed-text-v2:0", getRegion()),
                String.format("arn:aws:bedrock:%s::foundation-model/amazon.titan-text-lite-v1", getRegion())
            ))
            .build());

        // ========== ECS Cluster ==========
        Cluster cluster = Cluster.Builder.create(this, "DocumentServiceCluster")
            .clusterName(String.format("document-service-cluster-%s", environment))
            .vpc(vpc)
            .build();

        // ========== ECS Task Definition ==========
        TaskDefinition taskDefinition = TaskDefinition.Builder.create(this, "DocumentServiceTask")
            .compatibility(Compatibility.FARGATE)
            .cpu("1024")
            .memoryMiB("2048")
            .executionRole(ecsTaskExecutionRole)
            .taskRole(ecsTaskRole)
            .build();

        // Log group for ECS tasks
        LogGroup logGroup = LogGroup.Builder.create(this, "EcsLogGroup")
            .logGroupName(String.format("/ecs/document-service-backend-%s", environment))
            .retention(RetentionDays.ONE_WEEK)
            .removalPolicy(environment.equals("prod") ? 
                RemovalPolicy.RETAIN : RemovalPolicy.DESTROY)
            .build();

        // Container definition
        ContainerDefinition container = taskDefinition.addContainer("DocumentServiceContainer",
            ContainerDefinitionOptions.builder()
                .image(ContainerImage.fromRegistry("amazon/amazon-ecs-sample")) // Placeholder - will be updated via CI/CD
                .logging(LogDrivers.awsLogs(AwsLogDriverProps.builder()
                    .logGroup(logGroup)
                    .streamPrefix("ecs")
                    .build()))
                .environment(Map.of(
                    "SPRING_PROFILES_ACTIVE", environment,
                    "AWS_REGION", getRegion(),
                    "AWS_S3_BUCKET_NAME", documentsBucket.getBucketName(),
                    "AWS_DYNAMODB_TABLE_NAME", dynamoTable.getTableName(),
                    "AWS_BEDROCK_EMBEDDING_MODEL_ID", "amazon.titan-embed-text-v2:0",
                    "AWS_BEDROCK_LLM_MODEL_ID", "amazon.titan-text-lite-v1"
                ))
                .secrets(Map.of(
                    "SPRING_DATASOURCE_URL", 
                        Secret.fromSecretAttributes(this, "DbUrlSecret",
                            SecretAttributes.builder()
                                .secretArn(dbSecret.getSecretArn())
                                .build())
                        .secretValueFromJson("url"),
                    "SPRING_DATASOURCE_USERNAME",
                        Secret.fromSecretAttributes(this, "DbUsernameSecret",
                            SecretAttributes.builder()
                                .secretArn(dbSecret.getSecretArn())
                                .build())
                        .secretValueFromJson("username"),
                    "SPRING_DATASOURCE_PASSWORD",
                        Secret.fromSecretAttributes(this, "DbPasswordSecret",
                            SecretAttributes.builder()
                                .secretArn(dbSecret.getSecretArn())
                                .build())
                        .secretValueFromJson("password"),
                    "JWT_SECRET",
                        Secret.fromSecretAttributes(this, "JwtSecretAttr",
                            SecretAttributes.builder()
                                .secretArn(jwtSecret.getSecretArn())
                                .build())
                        .secretValueFromJson("secret")
                ))
                .build());

        container.addPortMappings(PortMapping.builder()
            .containerPort(8080)
            .protocol(Protocol.TCP)
            .build());

        // ========== Application Load Balancer ==========
        ApplicationLoadBalancer alb = ApplicationLoadBalancer.Builder.create(this, "DocumentServiceAlb")
            .vpc(vpc)
            .internetFacing(true)
            .securityGroup(albSecurityGroup)
            .build();

        // Target group
        ApplicationTargetGroup targetGroup = ApplicationTargetGroup.Builder.create(this, "DocumentServiceTargetGroup")
            .vpc(vpc)
            .port(8080)
            .protocol(ApplicationProtocol.HTTP)
            .targetType(TargetType.IP)
            .healthCheck(HealthCheck.builder()
                .path("/actuator/health")
                .healthyHttpCodes("200")
                .interval(Duration.seconds(30))
                .timeout(Duration.seconds(5))
                .build())
            .build();

        // HTTP listener (redirect to HTTPS)
        alb.addListener("HttpListener", BaseApplicationListenerProps.builder()
            .port(80)
            .protocol(ApplicationProtocol.HTTP)
            .defaultAction(ListenerAction.redirect(RedirectOptions.builder()
                .protocol("HTTPS")
                .port("443")
                .permanent(true)
                .build()))
            .build());

        // HTTPS listener (if certificate is provided)
        if (domainName != null) {
            // Note: Certificate needs to be created manually or via Route53
            // For now, we'll create a self-signed cert or use HTTP only
            // In production, use Certificate.fromCertificateArn() with ACM certificate
        }

        // HTTP listener for now (can be updated to HTTPS later)
        alb.addListener("HttpsListener", BaseApplicationListenerProps.builder()
            .port(443)
            .protocol(ApplicationProtocol.HTTP) // Change to HTTPS when certificate is ready
            .defaultTargetGroups(Arrays.asList(targetGroup))
            .build());

        // ========== ECS Service ==========
        FargateService service = FargateService.Builder.create(this, "DocumentService")
            .cluster(cluster)
            .taskDefinition(taskDefinition)
            .desiredCount(environment.equals("prod") ? 3 : 2)
            .securityGroups(Arrays.asList(ecsSecurityGroup))
            .assignPublicIp(false)
            .vpcSubnets(SubnetSelection.builder()
                .subnetType(SubnetType.PRIVATE_WITH_EGRESS)
                .build())
            .build();

        targetGroup.addTarget(service);

        // Auto-scaling
        ScalableTaskCount scalableTarget = service.getAutoScaleTaskCount(
            EnableScalingProps.builder()
                .minCapacity(environment.equals("prod") ? 2 : 1)
                .maxCapacity(environment.equals("prod") ? 10 : 5)
                .build());

        scalableTarget.scaleOnCpuUtilization("CpuScaling",
            CpuUtilizationScalingProps.builder()
                .targetUtilizationPercent(70)
                .build());

        scalableTarget.scaleOnMemoryUtilization("MemoryScaling",
            MemoryUtilizationScalingProps.builder()
                .targetUtilizationPercent(80)
                .build());

        // ========== CloudFront Distribution (Optional) ==========
        // For frontend static hosting
        Distribution cloudFront = Distribution.Builder.create(this, "FrontendDistribution")
            .defaultBehavior(BehaviorOptions.builder()
                .origin(new S3Origin(frontendBucket))
                .viewerProtocolPolicy(ViewerProtocolPolicy.REDIRECT_TO_HTTPS)
                .allowedMethods(AllowedMethods.ALLOW_GET_HEAD)
                .cachedMethods(CachedMethods.CACHE_GET_HEAD)
                .build())
            .comment("CloudFront distribution for Document Service frontend")
            .build();

        // ========== Outputs ==========
        CfnOutput.Builder.create(this, "VpcId")
            .value(vpc.getVpcId())
            .description("VPC ID")
            .exportName(String.format("DocumentServiceVpcId-%s", environment))
            .build();

        CfnOutput.Builder.create(this, "DatabaseEndpoint")
            .value(dbInstance.getInstanceEndpoint().getHostname())
            .description("RDS PostgreSQL endpoint")
            .exportName(String.format("DocumentServiceDbEndpoint-%s", environment))
            .build();

        CfnOutput.Builder.create(this, "AlbDnsName")
            .value(alb.getLoadBalancerDnsName())
            .description("Application Load Balancer DNS name")
            .exportName(String.format("DocumentServiceAlbDns-%s", environment))
            .build();

        CfnOutput.Builder.create(this, "CloudFrontUrl")
            .value(cloudFront.getDistributionDomainName())
            .description("CloudFront distribution URL")
            .exportName(String.format("DocumentServiceCloudFrontUrl-%s", environment))
            .build();

        CfnOutput.Builder.create(this, "DocumentsBucketName")
            .value(documentsBucket.getBucketName())
            .description("S3 bucket for documents")
            .exportName(String.format("DocumentServiceDocumentsBucket-%s", environment))
            .build();

        CfnOutput.Builder.create(this, "DbSecretArn")
            .value(dbSecret.getSecretArn())
            .description("Database secret ARN")
            .exportName(String.format("DocumentServiceDbSecret-%s", environment))
            .build();
    }

    /**
     * Properties for DocumentServiceStack
     */
    public static class DocumentServiceStackProps {
        private String environment;
        private String domainName;

        public String getEnvironment() {
            return environment;
        }

        public DocumentServiceStackProps setEnvironment(String environment) {
            this.environment = environment;
            return this;
        }

        public String getDomainName() {
            return domainName;
        }

        public DocumentServiceStackProps setDomainName(String domainName) {
            this.domainName = domainName;
            return this;
        }
    }
}
