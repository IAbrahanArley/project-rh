package br.com.abrahanarley.dto.request;

import br.com.abrahanarley.enums.ApplicationStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ApplicationStatusRequest(
		@NotNull ApplicationStatus status,
		@Size(max = 2000) String feedback) {
}
