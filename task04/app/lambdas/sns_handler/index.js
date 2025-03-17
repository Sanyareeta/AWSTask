// This is the Lambda handler for SNS messages
exports.handler = async (event) => {
    console.log("Received SNS message: ", JSON.stringify(event, null, 2));

    // Iterate over each record in the event (since SNS can trigger Lambda with multiple messages)
    for (const record of event.Records) {
        const snsMessage = record.Sns;
        console.log("SNS Message Body: ", snsMessage.Message);
    }

    return {
        statusCode: 200,
        body: JSON.stringify('Processed SNS message successfully!'),
    };
};

