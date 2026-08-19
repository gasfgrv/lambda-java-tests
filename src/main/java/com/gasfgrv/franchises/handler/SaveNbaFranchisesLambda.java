package com.gasfgrv.franchises.handler;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.amazonaws.services.lambda.runtime.logging.LogLevel;
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
    @SuppressWarnings("UseSpecificCatch")
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent request, Context context) {
        var logger = context.getLogger();

        try {
            if (request.getBody() == null || request.getBody().isBlank()) {
                logger.log("Request body is required", LogLevel.WARN);
                return responseFactory.json(400, Map.of("message", "Request body is required"));
            }

            var franchise = OBJECT_MAPPER.readValue(request.getBody(), Franchise.class);
            var validationErrors = validate(franchise);

            if (!validationErrors.isEmpty()) {
                logger.log("Error validating: " + validationErrors, LogLevel.WARN);
                return responseFactory.json(400, Map.of("message", validationErrors));
            }

            repository.save(franchise);

            return responseFactory.json(201, Map.of(
                    "message", "Franchise saved successfully",
                    "id", franchise.id()));
        } catch (FranchiseAlreadyExistsException exception) {
            logger.log(exception.getMessage(), LogLevel.WARN);
            return responseFactory.json(409, Map.of("message", exception.getMessage()));
        } catch (Exception exception) {
            logger.log("Error saving franchise: " + exception.getMessage(), LogLevel.ERROR);
            return responseFactory.json(500, Map.of("message", "Internal server error"));
        }
    }

    private Set<String> validate(Franchise franchise) {
        var errors = new HashSet<String>();

        if (franchise.id() == null || franchise.id().isBlank()) {
            errors.add("id is required");
        }

        if (franchise.name() == null || franchise.name().isBlank()) {
            errors.add("name is required");
        }

        if (franchise.foundationYear() == null || franchise.foundationYear() <= 0) {
            errors.add("foundationYear must be positive");
        }

        if (franchise.city() == null || franchise.city().isBlank()) {
            errors.add("city is required");
        }

        if (franchise.titles() == null || franchise.titles() < 0) {
            errors.add("titles must be zero or positive");
        }

        if (franchise.conferenceTitles() == null || franchise.conferenceTitles() < 0) {
            errors.add("conferenceTitles must be zero or positive");
        }

        if (franchise.conference() == null) {
            errors.add("conference is required");
        }

        return errors;
    }

}
