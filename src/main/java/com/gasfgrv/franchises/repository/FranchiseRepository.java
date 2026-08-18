package com.gasfgrv.franchises.repository;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.gasfgrv.franchises.exception.FranchiseAlreadyExistsException;
import com.gasfgrv.franchises.model.Conference;
import com.gasfgrv.franchises.model.Franchise;

import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.ConditionalCheckFailedException;

public class FranchiseRepository {

    private final DynamoDbClient dynamoDbClient;
    private final String tableName;

    public FranchiseRepository(DynamoDbClient dynamoDbClient, String tableName) {
        this.dynamoDbClient = dynamoDbClient;
        this.tableName = tableName;
    }

    public void save(Franchise franchise) {
        var item = new HashMap<String, AttributeValue>();
        item.put("id", AttributeValue.fromS(franchise.id()));
        item.put("name", AttributeValue.fromS(franchise.name()));
        item.put("foundationYear", AttributeValue.fromN(String.valueOf(franchise.foundationYear())));
        item.put("city", AttributeValue.fromS(franchise.city()));
        item.put("titles", AttributeValue.fromN(String.valueOf(franchise.titles())));
        item.put("conferenceTitles", AttributeValue.fromN(String.valueOf(franchise.conferenceTitles())));
        item.put("conference", AttributeValue.fromS(franchise.conference().name()));

        try {
            dynamoDbClient.putItem(request -> request.tableName(tableName)
                    .item(item)
                    .conditionExpression("attribute_not_exists(id)"));
        } catch (ConditionalCheckFailedException exception) {
            throw new FranchiseAlreadyExistsException(franchise.id());
        }
    }

    public Optional<Franchise> findById(String id) {
        var item = dynamoDbClient
                .getItem(request -> request.tableName(tableName).key(Map.of("id", AttributeValue.fromS(id))))
                .item();

        if (item == null || item.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(toFranchise(item));
    }

    private Franchise toFranchise(Map<String, AttributeValue> item) {
        return new Franchise(
                item.get("id").s(),
                item.get("name").s(),
                Integer.valueOf(item.get("foundationYear").n()),
                item.get("city").s(),
                Integer.valueOf(item.get("titles").n()),
                Integer.valueOf(item.get("conferenceTitles").n()),
                Conference.valueOf(item.get("conference").s()));
    }

}
