package br.com.abrahanarley.exceptions;

import java.time.OffsetDateTime;
import java.util.List;

public record ApiErrorResponse(
		OffsetDateTime timestamp,
		int status,
		String error,
		List<String> messages,
		String path) {

	public static ApiErrorResponse of(int status, String error, List<String> messages, String path) {
		return new ApiErrorResponse(OffsetDateTime.now(), status, error, messages, path);
	}
}
