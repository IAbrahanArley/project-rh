package br.com.abrahanarley.controllers;

import br.com.abrahanarley.dto.response.NotificationResponse;
import br.com.abrahanarley.dto.response.PageResponse;
import br.com.abrahanarley.dto.response.UnreadNotificationsResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.SortDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.UUID;

@Tag(name = "Notificacoes", description = "Endpoints para consulta e leitura das notificacoes do usuario autenticado.")
public interface NotificationApi {

	@Operation(
			summary = "Listar minhas notificacoes",
			description = "Retorna uma pagina de notificacoes do usuario autenticado, com filtro opcional por leitura.",
			security = @SecurityRequirement(name = "bearerAuth"))
	@ApiResponses({
			@ApiResponse(
					responseCode = "200",
					description = "Notificacoes retornadas com sucesso.",
					content = @Content(schema = @Schema(implementation = PageResponse.class))),
			@ApiResponse(responseCode = "400", description = "Parametro invalido.", content = @Content),
			@ApiResponse(responseCode = "401", description = "Token ausente, invalido ou expirado.", content = @Content)
	})
	@GetMapping("/me")
	PageResponse<NotificationResponse> listMine(
			@Parameter(description = "Filtra notificacoes por status de leitura.", example = "false")
			@RequestParam(required = false) Boolean read,
			@ParameterObject
			@PageableDefault(size = 10)
			@SortDefault(sort = "createdAt", direction = org.springframework.data.domain.Sort.Direction.DESC)
			Pageable pageable);

	@Operation(
			summary = "Contar notificacoes nao lidas",
			description = "Retorna a quantidade de notificacoes ainda nao lidas do usuario autenticado.",
			security = @SecurityRequirement(name = "bearerAuth"))
	@ApiResponses({
			@ApiResponse(
					responseCode = "200",
					description = "Contagem retornada com sucesso.",
					content = @Content(schema = @Schema(implementation = UnreadNotificationsResponse.class))),
			@ApiResponse(responseCode = "401", description = "Token ausente, invalido ou expirado.", content = @Content)
	})
	@GetMapping("/me/unread-count")
	UnreadNotificationsResponse countMineUnread();

	@Operation(
			summary = "Marcar notificacao como lida",
			description = "Marca uma notificacao do usuario autenticado como lida.",
			security = @SecurityRequirement(name = "bearerAuth"))
	@ApiResponses({
			@ApiResponse(
					responseCode = "200",
					description = "Notificacao marcada como lida.",
					content = @Content(schema = @Schema(implementation = NotificationResponse.class))),
			@ApiResponse(responseCode = "400", description = "ID invalido.", content = @Content),
			@ApiResponse(responseCode = "401", description = "Token ausente, invalido ou expirado.", content = @Content),
			@ApiResponse(responseCode = "404", description = "Notificacao nao encontrada.", content = @Content)
	})
	@PatchMapping("/{id}/read")
	NotificationResponse markAsRead(
			@Parameter(description = "Identificador publico da notificacao.",
					example = "1f8cc6c7-6dc4-4b45-97aa-5528ac1918ea")
			@PathVariable UUID id);

	@Operation(
			summary = "Marcar todas como lidas",
			description = "Marca todas as notificacoes nao lidas do usuario autenticado como lidas.",
			security = @SecurityRequirement(name = "bearerAuth"))
	@ApiResponses({
			@ApiResponse(
					responseCode = "200",
					description = "Notificacoes marcadas como lidas.",
					content = @Content(schema = @Schema(implementation = UnreadNotificationsResponse.class))),
			@ApiResponse(responseCode = "401", description = "Token ausente, invalido ou expirado.", content = @Content)
	})
	@PatchMapping("/read-all")
	UnreadNotificationsResponse markAllAsRead();
}
