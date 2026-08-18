package com.gasfgrv.franchises.handler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.gasfgrv.franchises.exception.FranchiseAlreadyExistsException;
import com.gasfgrv.franchises.model.Conference;
import com.gasfgrv.franchises.model.Franchise;
import com.gasfgrv.franchises.repository.FranchiseRepository;
import com.gasfgrv.franchises.response.ApiResponseFactory;

@ExtendWith(MockitoExtension.class)
public class SaveNbaFranchisesLambdaTest {

    @Mock
    private FranchiseRepository repository;

    @Mock
    private Context context;

    private SaveNbaFranchisesLambda lambda;

    @BeforeEach
    void setUp() {
        lambda = new SaveNbaFranchisesLambda(repository, new ApiResponseFactory());
    }

    @Test
    @DisplayName("Cenário 01: Salvar uma franquia com sucesso")
    void test_01() {
        var request = new APIGatewayProxyRequestEvent()
                .withBody("""
                        {
                          "id": "lal",
                          "name": "Los Angeles Lakers",
                          "foundationYear": 1947,
                          "city": "Los Angeles",
                          "titles": 17,
                          "conferenceTitles": 32,
                          "conference": "West"
                        }
                        """);

        var response = lambda.handleRequest(request, context);

        assertThat(response.getStatusCode()).isEqualTo(201);
        assertThat(response.getBody()).contains("Franchise saved successfully", "lal");

        var captor = ArgumentCaptor.forClass(Franchise.class);
        verify(repository).save(captor.capture());

        var franchise = captor.getValue();
        assertThat(franchise.id()).isEqualTo("lal");
        assertThat(franchise.name()).isEqualTo("Los Angeles Lakers");
        assertThat(franchise.foundationYear()).isEqualTo(1947);
        assertThat(franchise.city()).isEqualTo("Los Angeles");
        assertThat(franchise.titles()).isEqualTo(17);
        assertThat(franchise.conferenceTitles()).isEqualTo(32);
        assertThat(franchise.conference()).isEqualTo(Conference.West);
    }

    @Test
    @DisplayName("Cenário 02: Retornar 400 quando body não for informado")
    void test_02() {
        var request = new APIGatewayProxyRequestEvent();

        var response = lambda.handleRequest(request, context);

        assertThat(response.getStatusCode()).isEqualTo(400);
        assertThat(response.getBody()).contains("Request body is required");
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Cenário 03: Retornar 400 quando body estiver em branco")
    void test_03() {
        var request = new APIGatewayProxyRequestEvent().withBody(" ");

        var response = lambda.handleRequest(request, context);

        assertThat(response.getStatusCode()).isEqualTo(400);
        assertThat(response.getBody()).contains("Request body is required");
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Cenário 04: Retornar 400 quando payload for inválido")
    void test_04() {
        var request = new APIGatewayProxyRequestEvent()
                .withBody("""
                        {
                          "id": "",
                          "name": "Los Angeles Lakers",
                          "foundationYear": 1947,
                          "city": "Los Angeles",
                          "titles": 17,
                          "conferenceTitles": 32,
                          "conference": "West"
                        }
                        """);

        var response = lambda.handleRequest(request, context);

        assertThat(response.getStatusCode()).isEqualTo(400);
        assertThat(response.getBody()).contains("id is required");
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Cenário 05: Retornar 409 quando franquia já existir")
    void test_05() {
        doThrow(new FranchiseAlreadyExistsException("lal"))
                .when(repository)
                .save(any(Franchise.class));

        var request = new APIGatewayProxyRequestEvent()
                .withBody(validBody());

        var response = lambda.handleRequest(request, context);

        assertThat(response.getStatusCode()).isEqualTo(409);
        assertThat(response.getBody()).contains("Franchise already exists: lal");
        verify(repository).save(any(Franchise.class));
    }

    private String validBody() {
        return """
                {
                  "id": "lal",
                  "name": "Los Angeles Lakers",
                  "foundationYear": 1947,
                  "city": "Los Angeles",
                  "titles": 17,
                  "conferenceTitles": 32,
                  "conference": "West"
                }
                """;
    }

}
