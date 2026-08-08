package br.com.abrahanarley.controllers;

import br.com.abrahanarley.dto.request.ApplicationStatusRequest;
import br.com.abrahanarley.dto.request.EvaluationRequest;
import br.com.abrahanarley.dto.response.EvaluationResponse;
import br.com.abrahanarley.dto.response.JobApplicationResponse;
import br.com.abrahanarley.dto.response.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.SortDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.UUID;

@Tag(name = "Candidaturas", description = "Endpoints para acompanhamento, atualização e avaliação de candidaturas.")
public interface JobApplicationApi {

	@Operation(
			summary = "Listar minhas candidaturas",
			description = "Retorna uma página com as candidaturas do colaborador autenticado.",
			security = @SecurityRequirement(name = "bearerAuth"))
	@ApiResponses({
			@ApiResponse(
					responseCode = "200",
					description = "Candidaturas retornadas com sucesso.",
					content = @Content(schema = @Schema(implementation = PageResponse.class))),
			@ApiResponse(responseCode = "400", description = "Parametro inválido.", content = @Content),
			@ApiResponse(responseCode = "401", description = "Token ausente, inválido ou expirado.", content = @Content),
			@ApiResponse(responseCode = "403", description = "Usuário sem permissão para consultar candidaturas.",
					content = @Content)
	})
	@GetMapping("/me")
	PageResponse<JobApplicationResponse> listMine(
			@ParameterObject
			@PageableDefault(size = 10)
			@SortDefault(sort = "createdAt", direction = org.springframework.data.domain.Sort.Direction.DESC)
			Pageable pageable);

	@Operation(
			summary = "Atualizar status da candidatura",
			description = "Atualiza o status e o feedback de uma candidatura. Requer perfil de administrador.",
			security = @SecurityRequirement(name = "bearerAuth"))
	@ApiResponses({
			@ApiResponse(
					responseCode = "200",
					description = "Status atualizado com sucesso.",
					content = @Content(schema = @Schema(implementation = JobApplicationResponse.class))),
			@ApiResponse(responseCode = "400", description = "ID ou payload inválido.", content = @Content),
			@ApiResponse(responseCode = "401", description = "Token ausente, inválido ou expirado.", content = @Content),
			@ApiResponse(responseCode = "403", description = "Usuário sem permissão para atualizar status.",
					content = @Content),
			@ApiResponse(responseCode = "404", description = "Candidatura não encontrada.", content = @Content)
	})
	@PatchMapping("/{id}/status")
	JobApplicationResponse updateStatus(
			@Parameter(description = "Identificador público da candidatura.",
					example = "f9a0b59d-4302-4f0f-bb79-4f130f279099")
			@PathVariable UUID id,
			@RequestBody @Valid ApplicationStatusRequest request);

	@Operation(
			summary = "Avaliar candidatura",
			description = "Registra ou atualiza a avaliação de uma candidatura. Requer perfil de administrador.",
			security = @SecurityRequirement(name = "bearerAuth"))
	@ApiResponses({
			@ApiResponse(
					responseCode = "200",
					description = "Avaliação registrada com sucesso.",
					content = @Content(schema = @Schema(implementation = EvaluationResponse.class))),
			@ApiResponse(responseCode = "400", description = "ID ou payload inválido.", content = @Content),
			@ApiResponse(responseCode = "401", description = "Token ausente, inválido ou expirado.", content = @Content),
			@ApiResponse(responseCode = "403", description = "Usuário sem permissão para avaliar candidaturas.",
					content = @Content),
			@ApiResponse(responseCode = "404", description = "Candidatura não encontrada.", content = @Content)
	})
	@PostMapping("/{id}/evaluation")
	EvaluationResponse evaluate(
			@Parameter(description = "Identificador público da candidatura.",
					example = "f9a0b59d-4302-4f0f-bb79-4f130f279099")
			@PathVariable UUID id,
			@RequestBody @Valid EvaluationRequest request);
}
