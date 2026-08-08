package br.com.abrahanarley.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Credenciais para autenticação do usuário.")
public record LoginRequest(
		@Schema(description = "Nome de usuário.", example = "admin")
		@NotBlank String username,
		@Schema(description = "Senha do usuário.", example = "admin123")
		@NotBlank String password) {
}
