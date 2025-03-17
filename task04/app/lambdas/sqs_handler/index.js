// This is the Lambda handler for SQS messages
exports.handler = async (event) => {
    console.log("Received SQS message: ", JSON.stringify(event, null, 2));

    // Iterate over each record in the event (since SQS can batch messages)
    for (const record of event.Records) {
        console.log("Message body: ", record.body);
    }

    return {
        statusCode: 200,
        body: JSON.stringify('Processed SQS message successfully!'),
    };
};

