package br.com.abrahanarley.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Resumo de notificacoes nao lidas.")
public record UnreadNotificationsResponse(
		@Schema(description = "Quantidade de notificacoes nao lidas.", example = "3")
		long unreadCount) {
}
