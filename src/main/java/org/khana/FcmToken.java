package org.khana;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;

import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.PrimaryKey;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import org.khana.entity.Menu;
import org.khana.response.ApiResponse;

import java.util.HashMap;
import java.util.Map;

public class FcmToken implements RequestHandler<APIGatewayProxyRequestEvent, ApiResponse> {

    private static final AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard().withRegion(Regions.AP_SOUTH_1).build();
    @Override
    public ApiResponse handleRequest(APIGatewayProxyRequestEvent apiGatewayProxyRequestEvent, Context context) {
        Gson gson = new Gson();
        Menu menu = null;
        LambdaLogger logger = context.getLogger();
        try {
            menu = gson.fromJson(apiGatewayProxyRequestEvent.getBody(), Menu.class);
        } catch (JsonSyntaxException e) {
            logger.log(e.getMessage());
        }

        DynamoDB dynamoDB = new DynamoDB(client);
        Table table = dynamoDB.getTable("Restaurant");
        Map<String, String> expressionAttributeNames = new HashMap<String, String>();
        expressionAttributeNames.put("#T", "Token");

        Map<String, Object> expressionAttributeValues = new HashMap<String, Object>();
        expressionAttributeValues.put(":token", menu.getToken());
        table.updateItem (new PrimaryKey("RestaurantId", menu.getRestId(),"ItemName", "!"),
                "set #T = :token",
                expressionAttributeNames,
                expressionAttributeValues);

        Map<String,String> headers = Map.of("content-type", "application/json");
        return new ApiResponse(201, headers, "{\"status\":\"Updated\"}");

    }
}
