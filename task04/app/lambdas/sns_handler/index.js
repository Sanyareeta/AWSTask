//exports.handler = async (event) => {
//    // TODO implement
//    const response = {
//        statusCode: 200,
//        body: JSON.stringify('Hello from Lambda!'),
//    };
//    return response;
//};
const AWS = require('aws-sdk');

// Lambda handler function
exports.handler = async (event) => {
    console.log("Received SNS message: ", JSON.stringify(event));

    // Loop through each record from the SNS event
    for (const record of event.Records) {
        // Print the SNS message content to CloudWatch Logs
        console.log('SNS Message Body:', record.Sns.Message);
    }

    // Return success status
    return { statusCode: 200, body: 'Message processed successfully.' };
};
