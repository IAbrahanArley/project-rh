package br.com.abrahanarley.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(description = "Dados para gerar uma URL assinada de upload de currículo.")
public record ResumeUploadUrlRequest(
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
