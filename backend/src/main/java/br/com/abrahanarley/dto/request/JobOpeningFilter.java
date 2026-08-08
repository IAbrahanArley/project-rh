package br.com.abrahanarley.dto.request;

import br.com.abrahanarley.enums.JobStatus;

public record JobOpeningFilter(
		JobStatus status,
		String query,
		String department,
		String location) {
}
