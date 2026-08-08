package br.com.abrahanarley.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Resumo de notificações não lidas.")
public record UnreadNotificationsResponse(
		@Schema(description = "Quantidade de notificações não lidas.", example = "3")
		long unreadCount) {
}
