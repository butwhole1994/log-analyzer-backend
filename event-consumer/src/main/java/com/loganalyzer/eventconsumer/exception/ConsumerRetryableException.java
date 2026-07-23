package com.loganalyzer.eventconsumer.exception;

public class ConsumerRetryableException extends RuntimeException {

	public ConsumerRetryableException(String message, Throwable cause) {
		super(message, cause);
	}
}
