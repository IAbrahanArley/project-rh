package br.com.abrahanarley.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Credenciais para autenticacao do usuario.")
public record LoginRequest(
		@Schema(description = "Nome de usuario.", example = "admin")
		@NotBlank String username,
		@Schema(description = "Senha do usuario.", example = "admin123")
		@NotBlank String password) {
}
