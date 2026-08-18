package com.gasfgrv.franchises.response;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import com.gasfgrv.franchises.model.Conference;
import com.gasfgrv.franchises.model.Franchise;

@ExtendWith(MockitoExtension.class)
public class ApiResponseFactoryTest {

    private ApiResponseFactory factory;

    @BeforeEach
    void setUp() {
        factory = new ApiResponseFactory();
    }

    @Test
    @DisplayName("Cenário 01: montar json com sucesso")
    void test_01() {
        var franchise = new Franchise("lal",
                "Los Angeles Lakers",
                1947,
                "Los Angeles",
                17,
                32,
                Conference.West);

        var response = factory.json(200, franchise);

        assertThat(response.getStatusCode()).isBetween(200, 299)
                .isEqualTo(200);
        assertThat(response.getHeaders()).isNotEmpty()
                .contains(Map.entry("Content-Type", "application/json"));
        assertThat(response.getBody()).asString()
                .isNotBlank()
                .contains("lal", "Los Angeles Lakers", "1947", "Los Angeles", "17", "32", "West");
    }

    @Test
    @DisplayName("Cenário 02: Deve preservar o status code")
    void test_02() {
        var response = factory.json(201, Map.of(
                "message", "Franchise created"));

        assertThat(response.getStatusCode()).isBetween(200, 299)
                .isEqualTo(201);
        assertThat(response.getHeaders()).isNotEmpty()
                .contains(Map.entry("Content-Type", "application/json"));
        assertThat(response.getBody()).asString()
                .isNotBlank();
    }

    @Test
    @DisplayName("Cenário 03: Deve criar resposta de erro")
    void shouldCreateErrorResponse() {
        var response = factory.json(400, Map.of(
                "message", "Invalid request"));

        assertThat(response.getStatusCode()).isBetween(400, 499)
                .isEqualTo(400);
        assertThat(response.getHeaders()).isNotEmpty()
                .contains(Map.entry("Content-Type", "application/json"));
        assertThat(response.getBody()).asString()
                .contains("Invalid request");
    }

    @Test
    @DisplayName("Cenário 04: Deve retornar 500 quando não conseguir serializar")
    void test_04() {
        var body = new java.util.HashMap<String, Object>();
        body.put("message", "test");
        body.put("self", body);

        var response = factory.json(200, body);

        assertThat(response.getStatusCode()).isBetween(500, 599)
                .isEqualTo(500);
        assertThat(response.getHeaders()).isNotEmpty()
                .containsEntry("Content-Type", "application/json");
        assertThat(response.getBody()).asString()
                .isEqualTo("{\"message\": \"Failed to serialize response\"}");
    }

}
