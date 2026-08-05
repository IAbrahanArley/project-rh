package br.com.abrahanarley.dto.response;

public record AuthResponse(
		String tokenType,
		String accessToken,
		long expiresInSeconds,
		AuthenticatedUserResponse user) {
}
