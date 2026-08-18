package com.gasfgrv.franchises.handler;

import java.util.Map;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.gasfgrv.franchises.repository.FranchiseRepository;
import com.gasfgrv.franchises.response.ApiResponseFactory;
import com.gasfgrv.franchises.util.DynamoDbClientFactory;
import com.gasfgrv.franchises.util.EnvironmentResolver;

public class GetNbaFranchiseLambda
        implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private final FranchiseRepository repository;
    private final ApiResponseFactory responseFactory;

    public GetNbaFranchiseLambda() {
        var tableName = EnvironmentResolver.envOrDefault("TABLE_NAME", "tb-franchise");
        this.repository = new FranchiseRepository(DynamoDbClientFactory.create(), tableName);
        this.responseFactory = new ApiResponseFactory();
    }

    GetNbaFranchiseLambda(FranchiseRepository repository, ApiResponseFactory responseFactory) {
        this.repository = repository;
        this.responseFactory = responseFactory;
    }

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent request, Context context) {
        try {
            var queryParams = request.getQueryStringParameters();

            if (queryParams == null || !queryParams.containsKey("id") || queryParams.get("id").isBlank()) {
                return responseFactory.json(400, Map.of("message", "Query parameter id is required"));
            }

            var id = queryParams.get("id");

            return repository.findById(id)
                    .map(franchise -> responseFactory.json(200, franchise))
                    .orElseGet(() -> responseFactory.json(404, Map.of("message", "Franchise not found: " + id)));
        } catch (Exception exception) {
            context.getLogger().log("Error getting franchise: " + exception.getMessage());
            return responseFactory.json(500, Map.of("message", "Internal server error"));
        }
    }

}
