package br.com.abrahanarley.dto.response;

import br.com.abrahanarley.entities.CandidateResume;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.OffsetDateTime;
import java.util.UUID;

@Schema(description = "Metadados do currículo do candidato.")
public record ResumeResponse(
		@Schema(description = "Identificador público do currículo.")
		UUID id,
		@Schema(description = "Nome original do arquivo.", example = "currículo-maria.pdf")
		String fileName,
		@Schema(description = "Tipo MIME do arquivo.", example = "application/pdf")
		String contentType,
		@Schema(description = "Tamanho em bytes.", example = "245760")
		long sizeBytes,
		@Schema(description = "Data de envio do currículo.")
		OffsetDateTime uploadedAt) {

	public static ResumeResponse from(CandidateResume resume) {
		return new ResumeResponse(
				resume.getId(),
				resume.getFileName(),
				resume.getContentType(),
				resume.getSizeBytes(),
				resume.getUploadedAt());
	}
}
