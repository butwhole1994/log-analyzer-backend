package com.loganalyzer.logservice.controller;

import com.loganalyzer.logservice.dto.ApiResponse;
import com.loganalyzer.logservice.dto.LogSearchResponse;
import com.loganalyzer.logservice.service.LogSearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Log search API controller.
 *
 * @author butwhole1994
 */
@RestController
@RequestMapping("/api/logs")
@RequiredArgsConstructor
public class LogSearchController {

	private final LogSearchService logSearchService;

	@GetMapping("/search")
	public ApiResponse<LogSearchResponse> search() {
		return ApiResponse.success(logSearchService.search());
	}
}
