package br.com.abrahanarley.controllers;

import br.com.abrahanarley.dto.request.LoginRequest;
import br.com.abrahanarley.dto.response.AuthResponse;
import br.com.abrahanarley.dto.response.AuthenticatedUserResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "Autenticacao", description = "Endpoints para login JWT e consulta do usuario autenticado.")
public interface AuthApi {

	@Operation(
			summary = "Autenticar usuario",
			description = "Valida as credenciais do usuario e retorna um token JWT para consumo das rotas protegidas.")
	@ApiResponses({
			@ApiResponse(
					responseCode = "200",
					description = "Usuario autenticado com sucesso.",
					content = @Content(schema = @Schema(implementation = AuthResponse.class))),
			@ApiResponse(responseCode = "400", description = "Payload invalido.", content = @Content),
			@ApiResponse(responseCode = "401", description = "Credenciais invalidas.", content = @Content)
	})
	@PostMapping("/login")
	AuthResponse login(@RequestBody @Valid LoginRequest request);

	@Operation(
			summary = "Consultar usuario autenticado",
			description = "Retorna os dados basicos do usuario autenticado pelo token JWT.",
			security = @SecurityRequirement(name = "bearerAuth"))
	@ApiResponses({
			@ApiResponse(
					responseCode = "200",
					description = "Usuario autenticado retornado com sucesso.",
					content = @Content(schema = @Schema(implementation = AuthenticatedUserResponse.class))),
			@ApiResponse(responseCode = "401", description = "Token ausente, invalido ou expirado.", content = @Content)
	})
	@GetMapping("/me")
	AuthenticatedUserResponse me();
}
