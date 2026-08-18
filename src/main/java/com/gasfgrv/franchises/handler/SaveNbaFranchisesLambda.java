package com.gasfgrv.franchises.handler;

import java.util.Map;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gasfgrv.franchises.exception.FranchiseAlreadyExistsException;
import com.gasfgrv.franchises.model.Franchise;
import com.gasfgrv.franchises.repository.FranchiseRepository;
import com.gasfgrv.franchises.response.ApiResponseFactory;
import com.gasfgrv.franchises.util.DynamoDbClientFactory;
import com.gasfgrv.franchises.util.EnvironmentResolver;

public class SaveNbaFranchisesLambda
        implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final FranchiseRepository repository;
    private final ApiResponseFactory responseFactory;

    public SaveNbaFranchisesLambda() {
        var tableName = EnvironmentResolver.envOrDefault("TABLE_NAME", "tb-franchise");
        this.repository = new FranchiseRepository(DynamoDbClientFactory.create(), tableName);
        this.responseFactory = new ApiResponseFactory();
    }

    SaveNbaFranchisesLambda(FranchiseRepository repository, ApiResponseFactory responseFactory) {
        this.repository = repository;
        this.responseFactory = responseFactory;
    }

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent request, Context context) {
        try {
            if (request.getBody() == null || request.getBody().isBlank()) {
                return responseFactory.json(400, Map.of("message", "Request body is required"));
            }

            var franchise = OBJECT_MAPPER.readValue(request.getBody(), Franchise.class);
            var validationError = validate(franchise);

            if (validationError != null) {
                return responseFactory.json(400, Map.of("message", validationError));
            }

            repository.save(franchise);

            return responseFactory.json(201, Map.of(
                    "message", "Franchise saved successfully",
                    "id", franchise.id()));
        } catch (FranchiseAlreadyExistsException exception) {
            return responseFactory.json(409, Map.of("message", exception.getMessage()));
        } catch (Exception exception) {
            context.getLogger().log("Error saving franchise: " + exception.getMessage());
            return responseFactory.json(500, Map.of("message", "Internal server error"));
        }
    }

    private String validate(Franchise franchise) {
        if (franchise.id() == null || franchise.id().isBlank()) {
            return "id is required";
        }

        if (franchise.name() == null || franchise.name().isBlank()) {
            return "name is required";
        }

        if (franchise.foundationYear() == null || franchise.foundationYear() <= 0) {
            return "foundationYear must be positive";
        }

        if (franchise.city() == null || franchise.city().isBlank()) {
            return "city is required";
        }

        if (franchise.titles() == null || franchise.titles() < 0) {
            return "titles must be zero or positive";
        }

        if (franchise.conferenceTitles() == null || franchise.conferenceTitles() < 0) {
            return "conferenceTitles must be zero or positive";
        }

        if (franchise.conference() == null) {
            return "conference is required";
        }

        return null;
    }

}
