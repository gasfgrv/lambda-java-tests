package com.gasfgrv.franchises.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Map;
import java.util.function.Consumer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.gasfgrv.franchises.exception.FranchiseAlreadyExistsException;
import com.gasfgrv.franchises.model.Conference;
import com.gasfgrv.franchises.model.Franchise;

import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.ConditionalCheckFailedException;
import software.amazon.awssdk.services.dynamodb.model.GetItemResponse;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;

@ExtendWith(MockitoExtension.class)
public class FranchiseRepositoryTest {

    private static final String TABLE_NAME = "tb-franchise";

    @Mock
    private DynamoDbClient dynamoDbClient;

    private FranchiseRepository repository;

    @BeforeEach
    void setUp() {
        repository = new FranchiseRepository(dynamoDbClient, TABLE_NAME);
    }

    @Test
    @DisplayName("Cenário 01: Salvar uma franquia com sucesso")
    @SuppressWarnings("unchecked")
    void test_01() {
        var franchise = mountFranchise();

        repository.save(franchise);

        var captor = ArgumentCaptor.forClass(Consumer.class);
        verify(dynamoDbClient).putItem(captor.capture());

        var consumer = captor.getValue();
        var builder = PutItemRequest.builder();
        consumer.accept(builder);

        var request = builder.build();
        assertThat(request.tableName()).isEqualTo(TABLE_NAME);
        assertThat(request.conditionExpression()).isEqualTo("attribute_not_exists(id)");

        assertThat(request.item())
                .containsEntry("id", AttributeValue.fromS("lal"))
                .containsEntry("name", AttributeValue.fromS("Los Angeles Lakers"))
                .containsEntry("foundationYear", AttributeValue.fromN("1947"))
                .containsEntry("city", AttributeValue.fromS("Los Angeles"))
                .containsEntry("titles", AttributeValue.fromN("17"))
                .containsEntry("conferenceTitles", AttributeValue.fromN("32"))
                .containsEntry("conference", AttributeValue.fromS("West"));
    }

    @Test
    @DisplayName("Cenário 02: Lançar FranchiseAlreadyExistsException ao tentar salvar uma franquia já salva")
    @SuppressWarnings("unchecked")
    void test_02() {
        var franchise = mountFranchise();

        when(dynamoDbClient.putItem(any(Consumer.class)))
                .thenThrow(ConditionalCheckFailedException.builder().build());

        assertThatThrownBy(() -> repository.save(franchise))
                .isInstanceOf(FranchiseAlreadyExistsException.class);
    }

    @Test
    @DisplayName("Cenário 03: Obter uma uma franquia que já existe na base")
    @SuppressWarnings("unchecked")
    void test_03() {
        var item = Map.of(
                "id", AttributeValue.fromS("lal"),
                "name", AttributeValue.fromS("Los Angeles Lakers"),
                "foundationYear", AttributeValue.fromN("1947"),
                "city", AttributeValue.fromS("Los Angeles"),
                "titles", AttributeValue.fromN("17"),
                "conferenceTitles", AttributeValue.fromN("32"),
                "conference", AttributeValue.fromS("West"));

        when(dynamoDbClient.getItem(any(Consumer.class)))
                .thenReturn(GetItemResponse.builder().item(item).build());

        var result = repository.findById("lal");
        assertThat(result).isPresent();

        var franchise = result.get();
        assertThat(franchise.id()).isEqualTo("lal");
        assertThat(franchise.name()).isEqualTo("Los Angeles Lakers");
        assertThat(franchise.foundationYear()).isEqualTo(1947);
        assertThat(franchise.city()).isEqualTo("Los Angeles");
        assertThat(franchise.titles()).isEqualTo(17);
        assertThat(franchise.conferenceTitles()).isEqualTo(32);
        assertThat(franchise.conference()).isEqualTo(Conference.West);
    }

    @Test
    @DisplayName("Cenário 04: Tentar buscar uma franquia que não está salva")
    @SuppressWarnings("unchecked")
    void test_04() {
        when(dynamoDbClient.getItem(any(Consumer.class)))
                .thenReturn(GetItemResponse.builder().build());

        var result = repository.findById("lal");
        assertThat(result).isEmpty();
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
