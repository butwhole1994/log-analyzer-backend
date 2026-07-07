package com.loganalyzer.eventconsumer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * event-consumer 애플리케이션 진입점이다.
 *
 * @author butwhole1994
 */
@SpringBootApplication
public class EventConsumerApplication {

	public static void main(String[] args) {
		SpringApplication.run(EventConsumerApplication.class, args);
	}

}
