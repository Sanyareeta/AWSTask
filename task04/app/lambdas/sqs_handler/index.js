// Import the AWS SDK and other required modules
const AWS = require('aws-sdk');

// Lambda handler function
exports.handler = async (event) => {
    console.log("Received SQS message: ", JSON.stringify(event));

    // Loop through each record from the SQS event
    for (const record of event.Records) {
        // Print the message content (body of the SQS message) to CloudWatch Logs
        console.log('SQS Message Body:', record.body);
    }

    // Return success status
    return { statusCode: 200, body: 'Message processed successfully.' };
};

