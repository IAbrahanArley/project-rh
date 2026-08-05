package br.com.abrahanarley.controllers;

import br.com.abrahanarley.config.SecurityConfig;
import br.com.abrahanarley.dto.request.JobOpeningRequest;
import br.com.abrahanarley.dto.response.JobOpeningResponse;
import br.com.abrahanarley.enums.JobStatus;
import br.com.abrahanarley.exceptions.GlobalExceptionHandler;
import br.com.abrahanarley.security.JwtService;
import br.com.abrahanarley.services.JobApplicationService;
import br.com.abrahanarley.services.JobOpeningService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(JobOpeningController.class)
@Import({ SecurityConfig.class, GlobalExceptionHandler.class })
@TestPropertySource(properties = "app.cors.allowed-origins=http://localhost:4200")
class JobOpeningControllerWebMvcTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockitoBean
	private JobOpeningService jobOpeningService;

	@MockitoBean
	private JobApplicationService jobApplicationService;

	@MockitoBean
	private JwtService jwtService;

	@MockitoBean
	private UserDetailsService userDetailsService;

	@Test
	void listShouldReturnUnauthorizedWithoutAuthentication() throws Exception {
		mockMvc.perform(get("/api/jobs"))
				.andExpect(status().isUnauthorized());
	}

	@Test
	@WithMockUser(username = "colaborador", roles = "CANDIDATE")
	void createShouldReturnForbiddenForCandidate() throws Exception {
		JobOpeningRequest request = validRequest();

		mockMvc.perform(post("/api/jobs")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isForbidden());
	}

	@Test
	@WithMockUser(username = "admin", roles = "ADMIN")
	void createShouldReturnCreatedForAdmin() throws Exception {
		JobOpeningRequest request = validRequest();
		when(jobOpeningService.create(request)).thenReturn(new JobOpeningResponse(
				UUID.fromString("00000000-0000-0000-0000-000000000010"),
				"Analista Java",
				"Descricao da vaga",
				"Spring Boot",
				"Tecnologia",
				"Remoto",
				JobStatus.OPEN,
				"Administrador RH",
				OffsetDateTime.parse("2026-08-05T10:00:00Z"),
				OffsetDateTime.parse("2026-08-05T10:00:00Z")));

		mockMvc.perform(post("/api/jobs")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").value("00000000-0000-0000-0000-000000000010"))
				.andExpect(jsonPath("$.title").value("Analista Java"))
				.andExpect(jsonPath("$.status").value("OPEN"))
				.andExpect(jsonPath("$.createdBy").value("Administrador RH"));
	}

	@Test
	@WithMockUser(username = "admin", roles = "ADMIN")
	void createShouldReturnBadRequestWhenPayloadIsInvalid() throws Exception {
		JobOpeningRequest request = new JobOpeningRequest(
				"",
				"Descricao da vaga",
				"Spring Boot",
				"Tecnologia",
				"Remoto",
				JobStatus.OPEN);

		mockMvc.perform(post("/api/jobs")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.status").value(400))
				.andExpect(jsonPath("$.path").value("/api/jobs"));
	}

	@Test
	@WithMockUser(username = "admin", roles = "ADMIN")
	void listShouldReturnJobsForAuthenticatedUser() throws Exception {
		when(jobOpeningService.list(JobStatus.OPEN)).thenReturn(List.of(new JobOpeningResponse(
				UUID.fromString("00000000-0000-0000-0000-000000000010"),
				"Analista Java",
				"Descricao da vaga",
				"Spring Boot",
				"Tecnologia",
				"Remoto",
				JobStatus.OPEN,
				"Administrador RH",
				OffsetDateTime.parse("2026-08-05T10:00:00Z"),
				OffsetDateTime.parse("2026-08-05T10:00:00Z"))));

		mockMvc.perform(get("/api/jobs").param("status", "OPEN"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].id").value("00000000-0000-0000-0000-000000000010"))
				.andExpect(jsonPath("$[0].title").value("Analista Java"))
				.andExpect(jsonPath("$[0].status").value("OPEN"));
	}

	private JobOpeningRequest validRequest() {
		return new JobOpeningRequest(
				"Analista Java",
				"Descricao da vaga",
				"Spring Boot",
				"Tecnologia",
				"Remoto",
				JobStatus.OPEN);
	}
}
