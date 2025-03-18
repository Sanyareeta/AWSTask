
package com.task05;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.syndicate.deployment.annotations.environment.EnvironmentVariable;
import com.syndicate.deployment.annotations.environment.EnvironmentVariables;
import com.syndicate.deployment.annotations.lambda.LambdaHandler;
import com.syndicate.deployment.model.RetentionSetting;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@LambdaHandler(
		lambdaName = "api_handler",
		roleName = "api_handler-role",
		isPublishVersion = true,
		aliasName = "${lambdas_alias_name}",
		logsExpiration = RetentionSetting.SYNDICATE_ALIASES_SPECIFIED
)
@EnvironmentVariables(value = {
		@EnvironmentVariable(key = "target_table", value = "${target_table}") // Environment variable for the DynamoDB table name
})
public class ApiHandler implements RequestHandler<Map<String, Object>, Map<String, Object>> {


	private static final String TABLE_NAME = System.getenv("target_table");


	private static final AmazonDynamoDB amazonDynamoDB = AmazonDynamoDBClientBuilder.defaultClient();
	private static final DynamoDB dynamoDBClient = new DynamoDB(amazonDynamoDB);
	private static final ObjectMapper jsonMapper = new ObjectMapper();


	@Override
	public Map<String, Object> handleRequest(Map<String, Object> request, Context context) {
		context.getLogger().log("Using DynamoDB table: " + TABLE_NAME);
		context.getLogger().log("Processing request: " + request.toString());

		try {

			int principalId = (int) request.get("principalId");
			Map<String, String> content = (Map<String, String>) request.get("content");

			// Generate a unique event ID and current timestamp
			String eventId = UUID.randomUUID().toString();
			String createdAt = Instant.now().toString();


			Table table = dynamoDBClient.getTable(TABLE_NAME);
			Item eventItem = new Item()
					.withPrimaryKey("id", eventId)
					.withNumber("principalId", principalId)
					.withString("createdAt", createdAt)
					.withMap("body", content);


			table.putItem(eventItem);
			context.getLogger().log("Successfully saved event to DynamoDB: " + eventItem.toJSON());


			Map<String, Object> response = new HashMap<>();
			response.put("statusCode", 201);
			response.put("event", eventItem.toJSON());
			Map<String, String> headers = new HashMap<>();
			headers.put("Content-Type", "application/json");

			response.put("headers", headers);
			return response;
			return response;

		} catch (Exception e) {
			context.getLogger().log("Error occurred while processing the request: " + e.getMessage());


			Map<String, Object> errorResponse = new HashMap<>();
			errorResponse.put("statusCode", 500); // HTTP status code for 'Internal Server Error'
			errorResponse.put("error", "An error occurred while saving the event: " + e.getMessage());

			return errorResponse;
		}
	}
}
