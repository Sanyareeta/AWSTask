//exports.handler = async (event) => {
//    // TODO implement
//    const response = {
//        statusCode: 200,
//        body: JSON.stringify('Hello from Lambda!'),
//    };
//    return response;
//};
exports.handler = async (event) => {
    // Iterate through each record in the event (SQS message)
    for (const record of event.Records) {
        console.log('SQS Message Body:', record.body);  // Print the content of the SQS message to CloudWatch
    }
};

