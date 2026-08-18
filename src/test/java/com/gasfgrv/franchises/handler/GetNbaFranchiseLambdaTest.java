package com.gasfgrv.franchises.handler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.gasfgrv.franchises.model.Conference;
import com.gasfgrv.franchises.model.Franchise;
import com.gasfgrv.franchises.repository.FranchiseRepository;
import com.gasfgrv.franchises.response.ApiResponseFactory;

@ExtendWith(MockitoExtension.class)
public class GetNbaFranchiseLambdaTest {

    @Mock
    private FranchiseRepository repository;

    @Mock
    private Context context;

    private GetNbaFranchiseLambda lambda;

    @BeforeEach
    void setUp() {
        lambda = new GetNbaFranchiseLambda(repository, new ApiResponseFactory());
    }

    @Test
    @DisplayName("Cenário 01: Buscar uma franquia com sucesso")
    void test_01() {
        when(repository.findById("lal")).thenReturn(Optional.of(mountFranchise()));

        var request = new APIGatewayProxyRequestEvent()
                .withQueryStringParameters(Map.of("id", "lal"));

        var response = lambda.handleRequest(request, context);

        assertThat(response.getStatusCode()).isEqualTo(200);
        assertThat(response.getBody())
                .contains("lal", "Los Angeles Lakers", "1947", "Los Angeles", "17", "32", "West");
        verify(repository).findById("lal");
    }

    @Test
    @DisplayName("Cenário 02: Retornar 400 quando query params não forem informados")
    void test_02() {
        var request = new APIGatewayProxyRequestEvent();

        var response = lambda.handleRequest(request, context);

        assertThat(response.getStatusCode()).isEqualTo(400);
        assertThat(response.getBody()).contains("Query parameter id is required");
        verify(repository, never()).findById(anyString());
    }

    @Test
    @DisplayName("Cenário 03: Retornar 400 quando id não for informado")
    void test_03() {
        var request = new APIGatewayProxyRequestEvent()
                .withQueryStringParameters(Map.of("name", "Los Angeles Lakers"));

        var response = lambda.handleRequest(request, context);

        assertThat(response.getStatusCode()).isEqualTo(400);
        assertThat(response.getBody()).contains("Query parameter id is required");
        verify(repository, never()).findById(anyString());
    }

    @Test
    @DisplayName("Cenário 04: Retornar 400 quando id estiver em branco")
    void test_04() {
        var request = new APIGatewayProxyRequestEvent()
                .withQueryStringParameters(Map.of("id", " "));

        var response = lambda.handleRequest(request, context);

        assertThat(response.getStatusCode()).isEqualTo(400);
        assertThat(response.getBody()).contains("Query parameter id is required");
        verify(repository, never()).findById(anyString());
    }

    @Test
    @DisplayName("Cenário 05: Retornar 404 quando franquia não for encontrada")
    void test_05() {
        when(repository.findById("lal")).thenReturn(Optional.empty());

        var request = new APIGatewayProxyRequestEvent()
                .withQueryStringParameters(Map.of("id", "lal"));

        var response = lambda.handleRequest(request, context);

        assertThat(response.getStatusCode()).isEqualTo(404);
        assertThat(response.getBody()).contains("Franchise not found: lal");
        verify(repository).findById("lal");
    }

    private Franchise mountFranchise() {
        return new Franchise("lal",
                "Los Angeles Lakers",
                1947,
                "Los Angeles",
                17,
                32,
                Conference.West);
    }

}
