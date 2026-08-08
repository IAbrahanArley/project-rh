package br.com.abrahanarley.dto.request;

import br.com.abrahanarley.enums.JobStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "Dados para cadastro ou atualização de uma vaga interna.")
public record JobOpeningRequest(
		@Schema(description = "Titulo da vaga.", example = "Analista de Sistemas Pleno")
		@NotBlank @Size(max = 140) String title,
		@Schema(description = "Descrição das responsabilidades da vaga.", example = "Atuar no desenvolvimento de produtos internos.")
		@NotBlank @Size(max = 4000) String description,
		@Schema(description = "Requisitos técnicos e comportamentais da vaga.", example = "Java, Spring Boot, Angular e comunicação clara.")
		@NotBlank @Size(max = 4000) String requirements,
		@Schema(description = "Departamento responsável pela vaga.", example = "Tecnologia")
		@NotBlank @Size(max = 80) String department,
		@Schema(description = "Localidade ou modelo de trabalho da vaga.", example = "Recife - PE / Remoto")
		@NotBlank @Size(max = 80) String location,
		@Schema(description = "Status atual da vaga.", example = "OPEN")
		@NotNull JobStatus status) {
}
