package com.docmanagement.infrastructure;

import software.amazon.awscdk.App;
import software.amazon.awscdk.Environment;
import software.amazon.awscdk.StackProps;

/**
 * CDK App Entry Point
 * 
 * Usage:
 *   cdk deploy --all
 *   cdk deploy DocumentServiceStack-dev
 *   cdk deploy DocumentServiceStack-prod
 */
public class App {
    public static void main(final String[] args) {
        App app = new App();

        // Get environment from context or default to dev
        String environment = (String) app.getNode().tryGetContext("environment");
        if (environment == null) {
            environment = "dev";
        }

        // Get AWS account and region from environment or CDK context
        String accountId = System.getenv("CDK_DEFAULT_ACCOUNT");
        String region = System.getenv("CDK_DEFAULT_REGION");
        
        if (accountId == null || region == null) {
            // Use default AWS profile
            accountId = "123456789012"; // Replace with your account ID
            region = "us-east-1"; // Replace with your preferred region
        }

        Environment awsEnvironment = Environment.builder()
            .account(accountId)
            .region(region)
            .build();

        // Get domain name from context (optional)
        String domainName = (String) app.getNode().tryGetContext("domainName");

        // Create stack
        DocumentServiceStack.DocumentServiceStackProps stackProps = 
            new DocumentServiceStack.DocumentServiceStackProps()
                .setEnvironment(environment)
                .setDomainName(domainName);

        new DocumentServiceStack(app, 
            String.format("DocumentServiceStack-%s", environment),
            StackProps.builder()
                .env(awsEnvironment)
                .description(String.format("Document Management Service - %s", environment))
                .build(),
            stackProps);

        app.synth();
    }
}
