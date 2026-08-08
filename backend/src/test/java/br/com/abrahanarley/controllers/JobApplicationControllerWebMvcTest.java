package br.com.abrahanarley.controllers;

import br.com.abrahanarley.config.SecurityConfig;
import br.com.abrahanarley.dto.request.ApplicationStatusRequest;
import br.com.abrahanarley.dto.request.EvaluationRequest;
import br.com.abrahanarley.dto.response.EvaluationResponse;
import br.com.abrahanarley.dto.response.JobApplicationResponse;
import br.com.abrahanarley.enums.ApplicationStatus;
import br.com.abrahanarley.exceptions.GlobalExceptionHandler;
import br.com.abrahanarley.security.JwtService;
import br.com.abrahanarley.services.JobApplicationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = JobApplicationController.class)
@Import({ SecurityConfig.class, GlobalExceptionHandler.class })
@TestPropertySource(properties = "app.cors.allowed-origins=http://localhost:4200")
class JobApplicationControllerWebMvcTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockitoBean
	private JobApplicationService jobApplicationService;

	@MockitoBean
	private JwtService jwtService;

	@MockitoBean
	private UserDetailsService userDetailsService;

	@Test
	void listMineShouldReturnUnauthorizedWithoutAuthentication() throws Exception {
		mockMvc.perform(get("/api/applications/me"))
				.andExpect(status().isUnauthorized());
	}

	@Test
	@WithMockUser(username = "colaborador", roles = "CANDIDATE")
	void listMineShouldReturnPaginatedApplicationsForCandidate() throws Exception {
		when(jobApplicationService.listMine(any())).thenReturn(new PageImpl<>(List.of(applicationResponse()),
				PageRequest.of(0, 10), 1));

		mockMvc.perform(get("/api/applications/me")
						.param("page", "0")
						.param("size", "10"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.content[0].id").value("00000000-0000-0000-0000-000000000020"))
				.andExpect(jsonPath("$.content[0].jobTitle").value("Analista Java"))
				.andExpect(jsonPath("$.content[0].candidateName").value("Colaborador Teste"))
				.andExpect(jsonPath("$.content[0].status").value("PENDING"))
				.andExpect(jsonPath("$.totalElements").value(1))
				.andExpect(jsonPath("$.size").value(10))
				.andExpect(jsonPath("$.page").value(0))
				.andExpect(jsonPath("$.first").value(true))
				.andExpect(jsonPath("$.last").value(true));
	}

	@Test
	@WithMockUser(username = "admin", roles = "ADMIN")
	void updateStatusShouldReturnUpdatedApplicationForAdmin() throws Exception {
		UUID applicationId = UUID.fromString("00000000-0000-0000-0000-000000000020");
		ApplicationStatusRequest request = new ApplicationStatusRequest(ApplicationStatus.IN_REVIEW,
				"Perfil em analise.");
		when(jobApplicationService.updateStatus(eq(applicationId), any())).thenReturn(applicationResponse(
				ApplicationStatus.IN_REVIEW, "Perfil em analise."));

		mockMvc.perform(patch("/api/applications/{id}/status", applicationId)
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(applicationId.toString()))
				.andExpect(jsonPath("$.status").value("IN_REVIEW"))
				.andExpect(jsonPath("$.feedback").value("Perfil em analise."));
	}

	@Test
	@WithMockUser(username = "colaborador", roles = "CANDIDATE")
	void updateStatusShouldReturnForbiddenForCandidate() throws Exception {
		UUID applicationId = UUID.fromString("00000000-0000-0000-0000-000000000020");
		ApplicationStatusRequest request = new ApplicationStatusRequest(ApplicationStatus.APPROVED, null);

		mockMvc.perform(patch("/api/applications/{id}/status", applicationId)
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isForbidden());
	}

	@Test
	@WithMockUser(username = "admin", roles = "ADMIN")
	void evaluateShouldReturnEvaluationForAdmin() throws Exception {
		UUID applicationId = UUID.fromString("00000000-0000-0000-0000-000000000020");
		EvaluationRequest request = new EvaluationRequest(5, true, "Excelente aderencia.");
		when(jobApplicationService.evaluate(eq(applicationId), any())).thenReturn(new EvaluationResponse(
				UUID.fromString("00000000-0000-0000-0000-000000000030"),
				applicationId,
				"Administrador RH",
				5,
				true,
				"Excelente aderencia.",
				OffsetDateTime.parse("2026-08-05T10:00:00Z"),
				OffsetDateTime.parse("2026-08-05T10:00:00Z")));

		mockMvc.perform(post("/api/applications/{id}/evaluation", applicationId)
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.applicationId").value(applicationId.toString()))
				.andExpect(jsonPath("$.score").value(5))
				.andExpect(jsonPath("$.recommended").value(true))
				.andExpect(jsonPath("$.comments").value("Excelente aderencia."));
	}

	@Test
	@WithMockUser(username = "admin", roles = "ADMIN")
	void evaluateShouldReturnBadRequestWhenPayloadIsInvalid() throws Exception {
		UUID applicationId = UUID.fromString("00000000-0000-0000-0000-000000000020");
		EvaluationRequest request = new EvaluationRequest(6, true, "");

		mockMvc.perform(post("/api/applications/{id}/evaluation", applicationId)
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.status").value(400))
				.andExpect(jsonPath("$.path").value("/api/applications/" + applicationId + "/evaluation"));
	}

	private JobApplicationResponse applicationResponse() {
		return applicationResponse(ApplicationStatus.PENDING, null);
	}

	private JobApplicationResponse applicationResponse(ApplicationStatus status, String feedback) {
		return new JobApplicationResponse(
				UUID.fromString("00000000-0000-0000-0000-000000000020"),
				UUID.fromString("00000000-0000-0000-0000-000000000010"),
				"Analista Java",
				UUID.fromString("00000000-0000-0000-0000-000000000002"),
				"Colaborador Teste",
				"Tecnologia",
				"Quero crescer na empresa.",
				status,
				feedback,
				OffsetDateTime.parse("2026-08-05T10:00:00Z"),
				OffsetDateTime.parse("2026-08-05T10:00:00Z"));
	}
}
