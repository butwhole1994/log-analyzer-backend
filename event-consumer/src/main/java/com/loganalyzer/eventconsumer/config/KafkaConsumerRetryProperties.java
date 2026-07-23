package com.loganalyzer.eventconsumer.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.kafka.consumer.retry")
public record KafkaConsumerRetryProperties(
		long maxAttempts,
		long intervalMs
) {
}
