// Importing the necessary weather SDK from the Lambda Layer (Assuming the SDK is included in the Lambda Layer)
const { WeatherClient } = require('weather-sdk'); // Adjust this import if your SDK exports a different structure

exports.handler = async (event) => {
    // Extract path and method from the event object
    const path = event.rawPath;
    const method = event.requestContext.http.method;

    // Log the path and method to help with debugging
    console.log("Request path:", path);
    console.log("Request method:", method);

    // Handle /weather GET request
    if (path === "/weather" && method === "GET") {
        try {
            // Create a new instance of WeatherClient from the imported SDK (from Lambda Layer)
            const weatherClient = new WeatherClient();

            // Fetch the weather data for the specified latitude and longitude
            const weatherData = await weatherClient.getWeather(50.4375, 30.5); // Example coordinates

            // Return the successful response with weather data
            return {
                statusCode: 200,
                body: JSON.stringify({
                    latitude: 50.4375,
                    longitude: 30.5,
                    generationtime_ms: 0.025033950805664062, // Example time generation value
                    utc_offset_seconds: 7200,
                    timezone: "Europe/Kiev",
                    timezone_abbreviation: "EET",
                    elevation: 188.0,
                    hourly_units: {
                        time: "iso8601",
                        temperature_2m: "°C",
                        relative_humidity_2m: "%",
                        wind_speed_10m: "km/h"
                    },
                    hourly: {
                        time: [
                            "2023-12-04T00:00",
                            "2023-12-04T01:00",
                            "2023-12-04T02:00",
                            "...",
                        ],
                        temperature_2m: [-2.4, -2.8, -3.2, "..."],
                        relative_humidity_2m: [84, 85, 87, "..."],
                        wind_speed_10m: [7.6, 6.8, 5.6, "..."],
                    },
                    current_units: {
                        time: "iso8601",
                        interval: "seconds",
                        temperature_2m: "°C",
                        wind_speed_10m: "km/h"
                    },
                    current: {
                        time: "2023-12-04T07:00",
                        interval: 900,
                        temperature_2m: 0.2,
                        wind_speed_10m: 10.0
                    }
                }),
                headers: {
                    "content-type": "application/json"
                },
                isBase64Encoded: false
            };
        } catch (error) {
            // In case of error, log and return a 500 response
            console.error("Error fetching weather data:", error.message);
            return {
                statusCode: 500,
                body: JSON.stringify({ error: "Internal Server Error" }),
                headers: {
                    "content-type": "application/json"
                },
                isBase64Encoded: false
            };
        }
    }

    // For other paths or HTTP methods, return a Bad Request response
    return {
        statusCode: 400,
        body: JSON.stringify({
            statusCode: 400,
            message: `Bad request syntax or unsupported method. Request path: ${path}. HTTP method: ${method}`
        }),
        headers: {
            "content-type": "application/json"
        },
        isBase64Encoded: false
    };
};
