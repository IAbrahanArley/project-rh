package br.com.abrahanarley.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "URL temporária para baixar ou visualizar o currículo.")
public record ResumeDownloadUrlResponse(
		@Schema(description = "URL assinada para download do PDF.")
		String downloadUrl,
		@Schema(description = "Tempo de validade da URL em segundos.", example = "300")
		long expiresInSeconds) {
}
