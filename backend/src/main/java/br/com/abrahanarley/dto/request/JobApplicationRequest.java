package br.com.abrahanarley.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record JobApplicationRequest(
		@NotBlank @Size(max = 2000) String motivation) {
}
