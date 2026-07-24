package com.loganalyzer.logservice.exception;

/**
 * Exception raised when OpenSearch log search fails.
 *
 * @author butwhole1994
 */
public class OpenSearchSearchException extends RuntimeException {

	public OpenSearchSearchException(String message, Throwable cause) {
		super(message, cause);
	}
}
