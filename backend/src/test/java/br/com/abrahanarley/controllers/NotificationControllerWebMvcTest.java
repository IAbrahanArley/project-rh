package br.com.abrahanarley.controllers;

import br.com.abrahanarley.config.SecurityConfig;
import br.com.abrahanarley.dto.response.NotificationResponse;
import br.com.abrahanarley.dto.response.UnreadNotificationsResponse;
import br.com.abrahanarley.exceptions.GlobalExceptionHandler;
import br.com.abrahanarley.security.JwtService;
import br.com.abrahanarley.services.NotificationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = NotificationController.class)
@Import({ SecurityConfig.class, GlobalExceptionHandler.class })
@TestPropertySource(properties = "app.cors.allowed-origins=http://localhost:4200")
class NotificationControllerWebMvcTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private NotificationService notificationService;

	@MockitoBean
	private JwtService jwtService;

	@MockitoBean
	private UserDetailsService userDetailsService;

	@Test
	void listMineShouldReturnUnauthorizedWithoutAuthentication() throws Exception {
		mockMvc.perform(get("/api/notifications/me"))
				.andExpect(status().isUnauthorized());
	}

	@Test
	@WithMockUser(username = "colaborador", roles = "CANDIDATE")
	void listMineShouldReturnPagedNotificationsForAuthenticatedUser() throws Exception {
		when(notificationService.listMine(eq(false), any())).thenReturn(new PageImpl<>(List.of(notificationResponse()),
				PageRequest.of(0, 10), 1));

		mockMvc.perform(get("/api/notifications/me")
						.param("read", "false")
						.param("page", "0")
						.param("size", "10"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.content[0].id").value("00000000-0000-0000-0000-000000000040"))
				.andExpect(jsonPath("$.content[0].subject").value("Nova candidatura"))
				.andExpect(jsonPath("$.content[0].read").value(false))
				.andExpect(jsonPath("$.totalElements").value(1))
				.andExpect(jsonPath("$.size").value(10))
				.andExpect(jsonPath("$.page").value(0))
				.andExpect(jsonPath("$.first").value(true))
				.andExpect(jsonPath("$.last").value(true));
	}

	@Test
	@WithMockUser(username = "colaborador", roles = "CANDIDATE")
	void countMineUnreadShouldReturnUnreadCount() throws Exception {
		when(notificationService.countMineUnread()).thenReturn(new UnreadNotificationsResponse(3));

		mockMvc.perform(get("/api/notifications/me/unread-count"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.unreadCount").value(3));
	}

	@Test
	@WithMockUser(username = "colaborador", roles = "CANDIDATE")
	void markAsReadShouldReturnNotification() throws Exception {
		UUID notificationId = UUID.fromString("00000000-0000-0000-0000-000000000040");
		when(notificationService.markMineAsRead(notificationId)).thenReturn(new NotificationResponse(
				notificationId,
				"Nova candidatura",
				"Sua candidatura foi atualizada.",
				true,
				OffsetDateTime.parse("2026-08-05T10:00:00Z")));

		mockMvc.perform(patch("/api/notifications/{id}/read", notificationId))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(notificationId.toString()))
				.andExpect(jsonPath("$.read").value(true));
	}

	@Test
	@WithMockUser(username = "colaborador", roles = "CANDIDATE")
	void markAllAsReadShouldReturnZeroUnreadNotifications() throws Exception {
		when(notificationService.markAllMineAsRead()).thenReturn(new UnreadNotificationsResponse(0));

		mockMvc.perform(patch("/api/notifications/read-all"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.unreadCount").value(0));
	}

	private NotificationResponse notificationResponse() {
		return new NotificationResponse(
				UUID.fromString("00000000-0000-0000-0000-000000000040"),
				"Nova candidatura",
				"Sua candidatura foi atualizada.",
				false,
				OffsetDateTime.parse("2026-08-05T10:00:00Z"));
	}
}
