package br.com.abrahanarley.controllers;

import br.com.abrahanarley.dto.request.CompleteResumeUploadRequest;
import br.com.abrahanarley.dto.request.ResumeUploadUrlRequest;
import br.com.abrahanarley.dto.response.ResumeDownloadUrlResponse;
import br.com.abrahanarley.dto.response.ResumeResponse;
import br.com.abrahanarley.dto.response.ResumeUploadUrlResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.UUID;

@Tag(name = "Currículos", description = "Endpoints para upload e acesso seguro a currículos em PDF.")
public interface CandidateResumeApi {

	@Operation(
			summary = "Consultar meu currículo",
			description = "Retorna os metadados do currículo do candidato autenticado, quando existir.",
			security = @SecurityRequirement(name = "bearerAuth"))
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Currículo encontrado.",
					content = @Content(schema = @Schema(implementation = ResumeResponse.class))),
			@ApiResponse(responseCode = "204", description = "Candidato ainda não enviou currículo.", content = @Content),
			@ApiResponse(responseCode = "401", description = "Token ausente, inválido ou expirado.", content = @Content),
			@ApiResponse(responseCode = "403", description = "Usuário sem permissão.", content = @Content)
	})
	@GetMapping("/me/resume")
	ResponseEntity<ResumeResponse> findMine();

	@Operation(
			summary = "Gerar URL de upload do meu currículo",
			description = "Gera uma URL assinada temporária para upload direto de um PDF no bucket privado.",
			security = @SecurityRequirement(name = "bearerAuth"))
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "URL de upload gerada.",
					content = @Content(schema = @Schema(implementation = ResumeUploadUrlResponse.class))),
			@ApiResponse(responseCode = "400", description = "Payload inválido ou storage não configurado.", content = @Content),
			@ApiResponse(responseCode = "401", description = "Token ausente, inválido ou expirado.", content = @Content),
			@ApiResponse(responseCode = "403", description = "Usuário sem permissão.", content = @Content)
	})
	@PostMapping("/me/resume/upload-url")
	ResumeUploadUrlResponse createUploadUrl(@RequestBody @Valid ResumeUploadUrlRequest request);

	@Operation(
			summary = "Confirmar upload do meu currículo",
			description = "Confirma um upload ja feito no bucket e salva os metadados do PDF no banco.",
			security = @SecurityRequirement(name = "bearerAuth"))
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Currículo confirmado.",
					content = @Content(schema = @Schema(implementation = ResumeResponse.class))),
			@ApiResponse(responseCode = "400", description = "Payload inválido ou objeto incompatível.", content = @Content),
			@ApiResponse(responseCode = "401", description = "Token ausente, inválido ou expirado.", content = @Content),
			@ApiResponse(responseCode = "403", description = "Usuário sem permissão.", content = @Content)
	})
	@PostMapping("/me/resume/complete")
	ResumeResponse completeUpload(@RequestBody @Valid CompleteResumeUploadRequest request);

	@Operation(
			summary = "Gerar URL para meu currículo",
			description = "Gera uma URL assinada temporária para visualizar ou baixar o currículo do candidato autenticado.",
			security = @SecurityRequirement(name = "bearerAuth"))
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "URL de download gerada.",
					content = @Content(schema = @Schema(implementation = ResumeDownloadUrlResponse.class))),
			@ApiResponse(responseCode = "401", description = "Token ausente, inválido ou expirado.", content = @Content),
			@ApiResponse(responseCode = "403", description = "Usuário sem permissão.", content = @Content),
			@ApiResponse(responseCode = "404", description = "Currículo não encontrado.", content = @Content)
	})
	@GetMapping("/me/resume/download-url")
	ResumeDownloadUrlResponse createMineDownloadUrl();

	@Operation(
			summary = "Gerar URL de currículo de candidato",
			description = "Gera uma URL assinada temporária para o RH acessar o currículo de um candidato.",
			security = @SecurityRequirement(name = "bearerAuth"))
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "URL de download gerada.",
					content = @Content(schema = @Schema(implementation = ResumeDownloadUrlResponse.class))),
			@ApiResponse(responseCode = "401", description = "Token ausente, inválido ou expirado.", content = @Content),
			@ApiResponse(responseCode = "403", description = "Usuário sem permissão.", content = @Content),
			@ApiResponse(responseCode = "404", description = "Candidato ou currículo não encontrado.", content = @Content)
	})
	@GetMapping("/{candidateId}/resume/download-url")
	ResumeDownloadUrlResponse createCandidateDownloadUrl(
			@Parameter(description = "Identificador público do candidato.")
			@PathVariable UUID candidateId);
}
