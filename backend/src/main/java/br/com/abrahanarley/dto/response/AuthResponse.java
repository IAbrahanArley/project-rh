package br.com.abrahanarley.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Resposta de autenticacao com token JWT.")
public record AuthResponse(
		@Schema(description = "Tipo do token.", example = "Bearer")
		String tokenType,
		@Schema(description = "Token JWT usado no header Authorization.", example = "eyJhbGciOiJIUzM4NCJ9...")
		String accessToken,
		@Schema(description = "Tempo de expiracao do token em segundos.", example = "7200")
		long expiresInSeconds,
		@Schema(description = "Dados basicos do usuario autenticado.")
		AuthenticatedUserResponse user) {
}
