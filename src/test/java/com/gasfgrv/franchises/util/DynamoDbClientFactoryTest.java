package com.gasfgrv.franchises.util;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junitpioneer.jupiter.SetEnvironmentVariable;
import org.mockito.junit.jupiter.MockitoExtension;

import software.amazon.awssdk.regions.Region;

@ExtendWith(MockitoExtension.class)
public class DynamoDbClientFactoryTest {

    @Test
    @DisplayName("Cenário 01: Criando DynamoDbClient passando AWS_REGION")
    @SetEnvironmentVariable(key = "AWS_REGION", value = "sa-east-1")
    void test_01() {
        try (var client = DynamoDbClientFactory.create()) {
            assertThat(client).isNotNull();
            assertThat(client.serviceClientConfiguration().region())
                    .isEqualTo(Region.of("sa-east-1"));
        }
    }

    @Test
    @DisplayName("Cenário 02: Criando DynamoDbClient não passando AWS_REGION")
    void test_02() {
        try (var client = DynamoDbClientFactory.create()) {
            assertThat(client).isNotNull();
            assertThat(client.serviceClientConfiguration().region())
                    .isEqualTo(Region.of("us-east-1"));
        }
    }

    @Test
    @DisplayName("Cenário 03: Criando DynamoDbClient passando AWS_ENDPOINT_URL")
    @SetEnvironmentVariable(key = "AWS_ENDPOINT_URL", value = "http://localhost:4566")
    void test_03() {
        try (var client = DynamoDbClientFactory.create()) {
            assertThat(client).isNotNull();
            assertThat(client.serviceClientConfiguration().endpointOverride())
                    .isPresent()
                    .contains(java.net.URI.create("http://localhost:4566"));
        }
    }

    @Test
    @DisplayName("Cenário 04: Criando DynamoDbClient não passando AWS_ENDPOINT_URL")
    void test_04() {
        try (var client = DynamoDbClientFactory.create()) {
            assertThat(client).isNotNull();
            assertThat(client.serviceClientConfiguration().endpointOverride())
                    .isEmpty();
        }
    }

}
