package br.com.abrahanarley.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(description = "Dados para cadastro público de candidato.")
public record CandidateRegisterRequest(
		@Schema(description = "Nome de usuário para login.", example = "maria.silva")
		@NotBlank
		@Size(min = 3, max = 80)
		@Pattern(regexp = "^[A-Za-z0-9._-]+$", message = "must contain only letters, numbers, dots, underscores or hyphens")
		String username,
		@Schema(description = "Nome completo do candidato.", example = "Maria Silva")
		@NotBlank
		@Size(min = 3, max = 120)
		String fullName,
		@Schema(description = "E-mail do candidato.", example = "maria.silva@email.com")
		@NotBlank
		@Email
		@Size(max = 160)
		String email,
		@Schema(description = "Senha com pelo menos 8 caracteres, contendo letras e numeros.", example = "Senha123")
		@NotBlank
		@Size(min = 8, max = 72)
		@Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d).+$", message = "must contain at least one letter and one number")
		String password) {
}
