package br.com.abrahanarley.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "URL temporária para upload direto do currículo no bucket.")
public record ResumeUploadUrlResponse(
		@Schema(description = "URL assinada para envio HTTP PUT do PDF.")
		String uploadUrl,
		@Schema(description = "Chave privada do objeto no bucket.")
		String storageKey,
		@Schema(description = "Tempo de validade da URL em segundos.", example = "300")
		long expiresInSeconds,
		@Schema(description = "Content-Type obrigatório no upload.", example = "application/pdf")
		String requiredContentType) {
}
