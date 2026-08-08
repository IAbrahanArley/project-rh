package br.com.abrahanarley.controllers;

import br.com.abrahanarley.dto.request.JobApplicationRequest;
import br.com.abrahanarley.dto.request.JobOpeningFilter;
import br.com.abrahanarley.dto.request.JobOpeningRequest;
import br.com.abrahanarley.dto.response.JobApplicationResponse;
import br.com.abrahanarley.dto.response.JobOpeningResponse;
import br.com.abrahanarley.dto.response.PageResponse;
import br.com.abrahanarley.enums.JobStatus;
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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.UUID;

@Tag(name = "Vagas", description = "Endpoints para cadastro, consulta, atualização e cancelamento de vagas internas.")
public interface JobOpeningApi {

	@Operation(
			summary = "Listar vagas",
			description = "Retorna uma página de vagas internas, com filtros opcionais por status, texto, departamento e localidade.",
			security = @SecurityRequirement(name = "bearerAuth"))
	@ApiResponses({
			@ApiResponse(
					responseCode = "200",
					description = "Vagas retornadas com sucesso.",
					content = @Content(schema = @Schema(implementation = PageResponse.class))),
			@ApiResponse(responseCode = "400", description = "Parametro inválido.", content = @Content),
			@ApiResponse(responseCode = "401", description = "Token ausente, inválido ou expirado.", content = @Content)
	})
	@GetMapping
	PageResponse<JobOpeningResponse> list(
			@Parameter(description = "Filtra vagas por status.", example = "OPEN")
			@RequestParam(required = false) JobStatus status,
			@Parameter(description = "Busca por título, descrição ou requisitos.", example = "java")
			@RequestParam(required = false, name = "q") String query,
			@Parameter(description = "Filtra vagas por departamento.", example = "Tecnologia")
			@RequestParam(required = false) String department,
			@Parameter(description = "Filtra vagas por localidade.", example = "Remoto")
			@RequestParam(required = false) String location,
			@ParameterObject
			@PageableDefault(size = 10)
			@SortDefault(sort = "createdAt", direction = org.springframework.data.domain.Sort.Direction.DESC)
			Pageable pageable);

	@Operation(
			summary = "Buscar vaga por ID",
			description = "Retorna os dados de uma vaga interna pelo identificador público.",
			security = @SecurityRequirement(name = "bearerAuth"))
	@ApiResponses({
			@ApiResponse(
					responseCode = "200",
					description = "Vaga retornada com sucesso.",
					content = @Content(schema = @Schema(implementation = JobOpeningResponse.class))),
			@ApiResponse(responseCode = "400", description = "ID inválido.", content = @Content),
			@ApiResponse(responseCode = "401", description = "Token ausente, inválido ou expirado.", content = @Content),
			@ApiResponse(responseCode = "404", description = "Vaga não encontrada.", content = @Content)
	})
	@GetMapping("/{id}")
	JobOpeningResponse findById(
			@Parameter(description = "Identificador público da vaga.", example = "7f36b6d7-2f3d-4bb1-8d83-598a04fb9b4c")
			@PathVariable UUID id);

	@Operation(
			summary = "Cadastrar vaga",
			description = "Cria uma nova vaga interna. Requer perfil de administrador.",
			security = @SecurityRequirement(name = "bearerAuth"))
	@ApiResponses({
			@ApiResponse(
					responseCode = "201",
					description = "Vaga criada com sucesso.",
					content = @Content(schema = @Schema(implementation = JobOpeningResponse.class))),
			@ApiResponse(responseCode = "400", description = "Payload inválido.", content = @Content),
			@ApiResponse(responseCode = "401", description = "Token ausente, inválido ou expirado.", content = @Content),
			@ApiResponse(responseCode = "403", description = "Usuário sem permissão para cadastrar vagas.", content = @Content)
	})
	@PostMapping
	JobOpeningResponse create(@RequestBody @Valid JobOpeningRequest request);

	@Operation(
			summary = "Atualizar vaga",
			description = "Atualiza os dados de uma vaga interna existente. Requer perfil de administrador.",
			security = @SecurityRequirement(name = "bearerAuth"))
	@ApiResponses({
			@ApiResponse(
					responseCode = "200",
					description = "Vaga atualizada com sucesso.",
					content = @Content(schema = @Schema(implementation = JobOpeningResponse.class))),
			@ApiResponse(responseCode = "400", description = "ID ou payload inválido.", content = @Content),
			@ApiResponse(responseCode = "401", description = "Token ausente, inválido ou expirado.", content = @Content),
			@ApiResponse(responseCode = "403", description = "Usuário sem permissão para atualizar vagas.", content = @Content),
			@ApiResponse(responseCode = "404", description = "Vaga não encontrada.", content = @Content)
	})
	@PutMapping("/{id}")
	JobOpeningResponse update(
			@Parameter(description = "Identificador público da vaga.", example = "7f36b6d7-2f3d-4bb1-8d83-598a04fb9b4c")
			@PathVariable UUID id,
			@RequestBody @Valid JobOpeningRequest request);

	@Operation(
			summary = "Cancelar vaga",
			description = "Cancela uma vaga interna mantendo seu histórico. Requer perfil de administrador.",
			security = @SecurityRequirement(name = "bearerAuth"))
	@ApiResponses({
			@ApiResponse(responseCode = "204", description = "Vaga cancelada com sucesso.", content = @Content),
			@ApiResponse(responseCode = "400", description = "ID inválido.", content = @Content),
			@ApiResponse(responseCode = "401", description = "Token ausente, inválido ou expirado.", content = @Content),
			@ApiResponse(responseCode = "403", description = "Usuário sem permissão para cancelar vagas.", content = @Content),
			@ApiResponse(responseCode = "404", description = "Vaga não encontrada.", content = @Content)
	})
	@DeleteMapping("/{id}")
	void cancel(
			@Parameter(description = "Identificador público da vaga.", example = "7f36b6d7-2f3d-4bb1-8d83-598a04fb9b4c")
			@PathVariable UUID id);

	@Operation(
			summary = "Candidatar-se a uma vaga",
			description = "Registra a candidatura do colaborador autenticado em uma vaga aberta.",
			security = @SecurityRequirement(name = "bearerAuth"))
	@ApiResponses({
			@ApiResponse(
					responseCode = "201",
					description = "Candidatura registrada com sucesso.",
					content = @Content(schema = @Schema(implementation = JobApplicationResponse.class))),
			@ApiResponse(responseCode = "400", description = "Vaga fechada, candidatura duplicada ou payload inválido.",
					content = @Content),
			@ApiResponse(responseCode = "401", description = "Token ausente, inválido ou expirado.", content = @Content),
			@ApiResponse(responseCode = "403", description = "Usuário sem permissão para candidatar-se.",
					content = @Content),
			@ApiResponse(responseCode = "404", description = "Vaga não encontrada.", content = @Content)
	})
	@PostMapping("/{id}/applications")
	JobApplicationResponse apply(
			@Parameter(description = "Identificador público da vaga.", example = "7f36b6d7-2f3d-4bb1-8d83-598a04fb9b4c")
			@PathVariable UUID id,
			@RequestBody @Valid JobApplicationRequest request);

	@Operation(
			summary = "Listar candidaturas da vaga",
			description = "Retorna uma página com as candidaturas registradas em uma vaga. Requer perfil de administrador.",
			security = @SecurityRequirement(name = "bearerAuth"))
	@ApiResponses({
			@ApiResponse(
					responseCode = "200",
					description = "Candidaturas retornadas com sucesso.",
					content = @Content(schema = @Schema(implementation = PageResponse.class))),
			@ApiResponse(responseCode = "400", description = "ID ou parametro inválido.", content = @Content),
			@ApiResponse(responseCode = "401", description = "Token ausente, inválido ou expirado.", content = @Content),
			@ApiResponse(responseCode = "403", description = "Usuário sem permissão para listar candidaturas.",
					content = @Content),
			@ApiResponse(responseCode = "404", description = "Vaga não encontrada.", content = @Content)
	})
	@GetMapping("/{id}/applications")
	PageResponse<JobApplicationResponse> listApplications(
			@Parameter(description = "Identificador público da vaga.", example = "7f36b6d7-2f3d-4bb1-8d83-598a04fb9b4c")
			@PathVariable UUID id,
			@ParameterObject
			@PageableDefault(size = 10)
			@SortDefault(sort = "createdAt", direction = org.springframework.data.domain.Sort.Direction.DESC)
			Pageable pageable);
}
