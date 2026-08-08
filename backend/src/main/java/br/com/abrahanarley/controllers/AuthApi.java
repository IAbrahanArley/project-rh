package br.com.abrahanarley.controllers;

import br.com.abrahanarley.dto.request.CandidateRegisterRequest;
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

@Tag(name = "Autenticação", description = "Endpoints para login JWT e consulta do usuário autenticado.")
public interface AuthApi {

	@Operation(
			summary = "Cadastrar candidato",
			description = "Cria uma conta publica de candidato e retorna um token JWT para acesso ao portal.")
	@ApiResponses({
			@ApiResponse(
					responseCode = "200",
					description = "Candidato cadastrado e autenticado com sucesso.",
					content = @Content(schema = @Schema(implementation = AuthResponse.class))),
			@ApiResponse(responseCode = "400", description = "Payload inválido ou dados ja cadastrados.", content = @Content)
	})
	@PostMapping("/candidate/register")
	AuthResponse registerCandidate(@RequestBody @Valid CandidateRegisterRequest request);

	@Operation(
			summary = "Autenticar usuário",
			description = "Valida as credenciais do usuário e retorna um token JWT para consumo das rotas protegidas.")
	@ApiResponses({
			@ApiResponse(
					responseCode = "200",
					description = "Usuário autenticado com sucesso.",
					content = @Content(schema = @Schema(implementation = AuthResponse.class))),
			@ApiResponse(responseCode = "400", description = "Payload inválido.", content = @Content),
			@ApiResponse(responseCode = "401", description = "Credenciais inválidas.", content = @Content)
	})
	@PostMapping("/login")
	AuthResponse login(@RequestBody @Valid LoginRequest request);

	@Operation(
			summary = "Consultar usuário autenticado",
			description = "Retorna os dados básicos do usuário autenticado pelo token JWT.",
			security = @SecurityRequirement(name = "bearerAuth"))
	@ApiResponses({
			@ApiResponse(
					responseCode = "200",
					description = "Usuário autenticado retornado com sucesso.",
					content = @Content(schema = @Schema(implementation = AuthenticatedUserResponse.class))),
			@ApiResponse(responseCode = "401", description = "Token ausente, inválido ou expirado.", content = @Content)
	})
	@GetMapping("/me")
	AuthenticatedUserResponse me();
}
