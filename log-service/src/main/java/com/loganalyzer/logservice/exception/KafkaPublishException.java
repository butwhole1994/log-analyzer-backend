package com.loganalyzer.logservice.exception;

/**
 * 로그 이벤트를 Kafka로 발행하지 못했을 때 사용하는 서비스 예외다.
 *
 * @author butwhole1994
 */
public class KafkaPublishException extends RuntimeException {

	/**
	 * 발행 실패 메시지와 원인 예외를 보존한다.
	 *
	 * @param message 발행 실패 메시지
	 * @param cause 실제 실패 원인
	 */
	public KafkaPublishException(String message, Throwable cause) {
		super(message, cause);
	}
}
