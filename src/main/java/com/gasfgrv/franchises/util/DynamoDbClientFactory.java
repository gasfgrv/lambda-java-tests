package com.gasfgrv.franchises.util;

import java.net.URI;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

public class DynamoDbClientFactory {

    private DynamoDbClientFactory() {
    }

    public static DynamoDbClient create() {
        var region = EnvironmentResolver.envOrDefault("AWS_REGION", "us-east-1");
        var accessKey = EnvironmentResolver.envOrDefault("AWS_ACCESS_KEY", "test");
        var secretKey = EnvironmentResolver.envOrDefault("AWS_SECRET_KEY", "test");
        var endpoint = System.getenv("AWS_ENDPOINT_URL");

        var builder = DynamoDbClient.builder()
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKey, secretKey)))
                .httpClientBuilder(UrlConnectionHttpClient.builder());

        if (endpoint != null && !endpoint.isBlank()) {
            builder.endpointOverride(URI.create(endpoint));
        }

        return builder.build();
    }

}
