package com.loganalyzer.eventconsumer.config;

import com.loganalyzer.eventconsumer.exception.ConsumerNonRetryableException;
import com.loganalyzer.eventconsumer.exception.ConsumerRetryableException;
import com.loganalyzer.eventconsumer.service.LogEventDlqPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.boot.autoconfigure.kafka.ConcurrentKafkaListenerContainerFactoryConfigurer;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.listener.RetryListener;
import org.springframework.util.backoff.FixedBackOff;

@Configuration
@Slf4j
@RequiredArgsConstructor
@EnableConfigurationProperties(KafkaConsumerRetryProperties.class)
public class KafkaConsumerRetryConfig {

	private final KafkaConsumerRetryProperties retryProperties;
	private final LogEventDlqPublisher dlqPublisher;

	@Bean
	public ConcurrentKafkaListenerContainerFactory<Object, Object> kafkaListenerContainerFactory(
			ConcurrentKafkaListenerContainerFactoryConfigurer configurer,
			ConsumerFactory<Object, Object> consumerFactory
	) {
		ConcurrentKafkaListenerContainerFactory<Object, Object> factory =
				new ConcurrentKafkaListenerContainerFactory<>();
		configurer.configure(factory, consumerFactory);
		factory.setCommonErrorHandler(kafkaConsumerErrorHandler());
		return factory;
	}

	@Bean
	public DefaultErrorHandler kafkaConsumerErrorHandler() {
		DefaultErrorHandler errorHandler = new DefaultErrorHandler(
				(record, exception) -> dlqPublisher.publish(record, exception),
				new FixedBackOff(retryProperties.intervalMs(), retryProperties.maxAttempts())
		);
		errorHandler.addRetryableExceptions(ConsumerRetryableException.class);
		errorHandler.addNotRetryableExceptions(
				ConsumerNonRetryableException.class,
				IllegalArgumentException.class
		);
		errorHandler.setRetryListeners(new RetryListener() {
			@Override
			public void failedDelivery(ConsumerRecord<?, ?> record, Exception exception, int deliveryAttempt) {
				log.warn(
						"Retrying Kafka log event processing: topic={}, partition={}, offset={}, deliveryAttempt={}, maxAttempts={}, failureType={}, failureMessage={}",
						record.topic(),
						record.partition(),
						record.offset(),
						deliveryAttempt,
						retryProperties.maxAttempts(),
						exception.getClass().getName(),
						exception.getMessage()
				);
			}
		});
		return errorHandler;
	}
}
