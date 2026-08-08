package br.com.abrahanarley.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(description = "Dados para confirmar um upload de currículo ja enviado ao bucket.")
public record CompleteResumeUploadRequest(
		@Schema(description = "Chave do objeto gerada pelo backend.", example = "resumes/00000000-0000-0000-0000-000000000002/8f7a.pdf")
		@NotBlank
		@Size(max = 500)
		String storageKey,
		@Schema(description = "Nome original do arquivo PDF.", example = "currículo-maria.pdf")
		@NotBlank
		@Size(max = 180)
		@Pattern(regexp = "(?i)^.+\\.pdf$", message = "must be a PDF file name")
		String fileName,
		@Schema(description = "Tipo MIME do arquivo.", example = "application/pdf")
		@NotBlank
		@Pattern(regexp = "^application/pdf$", message = "must be application/pdf")
		String contentType,
		@Schema(description = "Tamanho do arquivo em bytes.", example = "245760")
		@Min(1)
		@Max(10485760)
		long sizeBytes) {
}
