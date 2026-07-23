package com.loganalyzer.eventconsumer.controller;

import com.loganalyzer.eventconsumer.dto.ApiResponse;
import com.loganalyzer.eventconsumer.dto.LogEventConsumeStatusResponse;
import com.loganalyzer.eventconsumer.service.LogEventConsumeStatusService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Kafka 로그 이벤트 consume 상태를 확인하는 HTTP API 컨트롤러다.
 *
 * @author butwhole1994
 */
@RestController
@RequestMapping("/api/consumer/log-events")
@RequiredArgsConstructor
public class LogEventConsumeStatusController {

	private final LogEventConsumeStatusService consumeStatusService;

	/**
	 * 애플리케이션 기동 이후 Kafka 로그 이벤트 consume 성공 상태를 반환한다.
	 *
	 * @return 표준 응답 봉투로 감싼 consume 상태
	 */
	@GetMapping("/status")
	public ApiResponse<LogEventConsumeStatusResponse> getStatus() {
		return ApiResponse.success(consumeStatusService.getStatus());
	}
}
