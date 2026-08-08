package br.com.abrahanarley.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record EvaluationRequest(
		@NotNull @Min(1) @Max(5) Integer score,
		boolean recommended,
		@NotBlank @Size(max = 2000) String comments) {
}
