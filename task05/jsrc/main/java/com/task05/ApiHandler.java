//package com.task05;
//
//import com.amazonaws.services.lambda.runtime.Context;
//import com.amazonaws.services.lambda.runtime.RequestHandler;
//import com.syndicate.deployment.annotations.lambda.LambdaHandler;
//import com.syndicate.deployment.model.RetentionSetting;
//
//import java.util.HashMap;
//import java.util.Map;
//
//@LambdaHandler(
//    lambdaName = "api_handler",
//	roleName = "api_handler-role",
//	isPublishVersion = true,
//	aliasName = "${lambdas_alias_name}",
//	logsExpiration = RetentionSetting.SYNDICATE_ALIASES_SPECIFIED
//)
//public class ApiHandler implements RequestHandler<Object, Map<String, Object>> {
//
//	public Map<String, Object> handleRequest(Object request, Context context) {
//		System.out.println("Hello from lambda");
//		Map<String, Object> resultMap = new HashMap<String, Object>();
//		resultMap.put("statusCode", 200);
//		resultMap.put("body", "Hello from Lambda");
//		return resultMap;
//	}
//}
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
		lambdaName = "api_handler", // Lambda function name
		roleName = "api_handler-role", // IAM Role name associated with the Lambda
		isPublishVersion = true, // Automatically publish the version
		aliasName = "${lambdas_alias_name}", // Alias for the Lambda function
		logsExpiration = RetentionSetting.SYNDICATE_ALIASES_SPECIFIED // Log retention setting
)
@EnvironmentVariables(value = {
		@EnvironmentVariable(key = "target_table", value = "${target_table}") // Environment variable for the DynamoDB table name
})
public class ApiHandler implements RequestHandler<Map<String, Object>, Map<String, Object>> {

	// Fetch the DynamoDB table name from environment variables
	private static final String TABLE_NAME = System.getenv("target_table");

	// Initialize the DynamoDB client and document client
	private static final AmazonDynamoDB amazonDynamoDB = AmazonDynamoDBClientBuilder.defaultClient();
	private static final DynamoDB dynamoDBClient = new DynamoDB(amazonDynamoDB);
	private static final ObjectMapper jsonMapper = new ObjectMapper();

	/**
	 * The Lambda function handler that processes incoming requests and saves events to DynamoDB.
	 *
	 * @param request  The input request map containing 'principalId' and 'content' data.
	 * @param context The Lambda context object to get metadata about the function's execution.
	 * @return A response map containing the HTTP status and the saved event data.
	 */
	@Override
	public Map<String, Object> handleRequest(Map<String, Object> request, Context context) {
		context.getLogger().log("Processing request: " + request.toString());

		try {
			// Extract 'principalId' and 'content' from the incoming request
			int principalId = (int) request.get("principalId");
			Map<String, String> content = (Map<String, String>) request.get("content");

			// Generate a unique event ID and current timestamp
			String eventId = UUID.randomUUID().toString();
			String createdAt = Instant.now().toString();

			// Prepare the item to be stored in DynamoDB
			Table table = dynamoDBClient.getTable(TABLE_NAME);
			Item eventItem = new Item()
					.withPrimaryKey("id", eventId) // Primary key 'id' with a UUID
					.withNumber("principalId", principalId) // Store the principalId as a number
					.withString("createdAt", createdAt) // Store the creation time in ISO 8601 format
					.withMap("body", content); // Store the content as a map

			// Save the item to DynamoDB
			table.putItem(eventItem);
			context.getLogger().log("Successfully saved event to DynamoDB: " + eventItem.toJSON());

			// Create and return the response with the event data
			Map<String, Object> response = new HashMap<>();
			response.put("statusCode", 201); // HTTP status code for 'Created'
			response.put("event", eventItem.asMap()); // Return the event details in the response

			return response;

		} catch (Exception e) {
			context.getLogger().log("Error occurred while processing the request: " + e.getMessage());

			// Handle any errors and return a 500 status code with the error message
			Map<String, Object> errorResponse = new HashMap<>();
			errorResponse.put("statusCode", 500); // HTTP status code for 'Internal Server Error'
			errorResponse.put("error", "An error occurred while saving the event: " + e.getMessage());

			return errorResponse;
		}
	}
}
