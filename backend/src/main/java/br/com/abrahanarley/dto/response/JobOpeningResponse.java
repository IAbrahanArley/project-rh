package br.com.abrahanarley.dto.response;

import br.com.abrahanarley.entities.JobOpening;
import br.com.abrahanarley.enums.JobStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.OffsetDateTime;
import java.util.UUID;

@Schema(description = "Dados de uma vaga interna.")
public record JobOpeningResponse(
		@Schema(description = "Identificador público da vaga.", example = "7f36b6d7-2f3d-4bb1-8d83-598a04fb9b4c")
		UUID id,
		@Schema(description = "Titulo da vaga.", example = "Analista de Sistemas Pleno")
		String title,
		@Schema(description = "Descrição das responsabilidades da vaga.", example = "Atuar no desenvolvimento de produtos internos.")
		String description,
		@Schema(description = "Requisitos técnicos e comportamentais da vaga.", example = "Java, Spring Boot, Angular e comunicação clara.")
		String requirements,
		@Schema(description = "Departamento responsável pela vaga.", example = "Tecnologia")
		String department,
		@Schema(description = "Localidade ou modelo de trabalho da vaga.", example = "Recife - PE / Remoto")
		String location,
		@Schema(description = "Status atual da vaga.", example = "OPEN")
		JobStatus status,
		@Schema(description = "Nome do administrador que cadastrou a vaga.", example = "Administrador RH")
		String createdBy,
		@Schema(description = "Data e hora de criação da vaga.", example = "2026-08-05T10:00:00Z")
		OffsetDateTime createdAt,
		@Schema(description = "Data e hora da última atualização da vaga.", example = "2026-08-05T10:00:00Z")
		OffsetDateTime updatedAt) {

	public static JobOpeningResponse from(JobOpening job) {
		return new JobOpeningResponse(
				job.getId(),
				job.getTitle(),
				job.getDescription(),
				job.getRequirements(),
				job.getDepartment(),
				job.getLocation(),
				job.getStatus(),
				job.getCreatedBy().getFullName(),
				job.getCreatedAt(),
				job.getUpdatedAt());
	}
}
