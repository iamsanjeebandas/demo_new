package com.task09;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.syndicate.deployment.annotations.lambda.LambdaHandler;
import com.syndicate.deployment.annotations.lambda.LambdaLayer;
import com.syndicate.deployment.annotations.lambda.LambdaUrlConfig;
import com.syndicate.deployment.enums.ArtifactExtension;
import com.syndicate.deployment.enums.AuthType;
import com.syndicate.deployment.enums.DeploymentRuntime;
import com.syndicate.deployment.enums.InvokeMode;
import com.syndicate.deployment.model.RetentionSetting;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@LambdaHandler(
		lambdaName = "api_handler",
		roleName = "api_handler-role",
		isPublishVersion = true,
		aliasName = "${lambdas_alias_name}",
		logsExpiration = RetentionSetting.SYNDICATE_ALIASES_SPECIFIED
)
@LambdaLayer(
		layerName = "weather_sdk",
		libraries = {"lib/weather-sdk-1.0.0.jar"},
		runtime = DeploymentRuntime.JAVA11,
		artifactExtension = ArtifactExtension.ZIP
)
@LambdaUrlConfig(
		authType = AuthType.NONE,
		invokeMode = InvokeMode.BUFFERED
)
public class ApiHandler implements RequestHandler<Map<String, Object>, Map<String, Object>> {

	@Override
	public Map<String, Object> handleRequest(Map<String, Object> event, Context context) {
		String path = (String) event.get("rawPath");
		Map<String, Object> requestContext = (Map<String, Object>) event.get("requestContext");
		String method = (String) ((Map<String, Object>) requestContext.get("http")).get("method");

		if ("/weather".equals(path) && "GET".equalsIgnoreCase(method)) {
			Map<String, Object> weatherData = fetchWeatherData();
			return Map.of(
					"statusCode", 200,
					"body", weatherData,
					"headers", Map.of("content-type", "application/json"),
					"isBase64Encoded", false
			);
		}

		return Map.of(
				"statusCode", 400,
				"body", Map.of(
						"statusCode", 400,
						"message", String.format(
								"Bad request syntax or unsupported method. Request path: %s. HTTP method: %s",
								path, method
						)
				),
				"headers", Map.of("content-type", "application/json"),
				"isBase64Encoded", false
		);
	}

	private Map<String, Object> fetchWeatherData() {
		return Map.of(
				"latitude", 50.4375,
				"longitude", 30.5,
				"generationtime_ms", 0.025033950805664062,
				"utc_offset_seconds", 7200,
				"timezone", "Europe/Kiev",
				"timezone_abbreviation", "EET",
				"elevation", 188.0,
				"hourly_units", Map.of(
						"time", "iso8601",
						"temperature_2m", "°C",
						"relative_humidity_2m", "%",
						"wind_speed_10m", "km/h"
				),
				"hourly", Map.of(
						"time", List.of("2023-12-04T00:00", "2023-12-04T01:00", "2023-12-04T02:00", "..."),
						"temperature_2m", List.of(-2.4, -2.8, -3.2, "..."),
						"relative_humidity_2m", List.of(84, 85, 87, "..."),
						"wind_speed_10m", List.of(7.6, 6.8, 5.6, "...")
				),
				"current_units", Map.of(
						"time", "iso8601",
						"interval", "seconds",
						"temperature_2m", "°C",
						"wind_speed_10m", "km/h"
				),
				"current", Map.of(
						"time", "2023-12-04T07:00",
						"interval", 900,
						"temperature_2m", 0.2,
						"wind_speed_10m", 10.0
				)
		);
	}
}