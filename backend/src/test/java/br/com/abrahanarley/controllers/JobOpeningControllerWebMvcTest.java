package br.com.abrahanarley.controllers;

import br.com.abrahanarley.dto.request.JobOpeningFilter;
import br.com.abrahanarley.config.SecurityConfig;
import br.com.abrahanarley.dto.request.JobOpeningRequest;
import br.com.abrahanarley.dto.response.JobApplicationResponse;
import br.com.abrahanarley.dto.response.JobOpeningResponse;
import br.com.abrahanarley.enums.ApplicationStatus;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = JobOpeningController.class)
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
	void listShouldReturnJobsWithoutAuthentication() throws Exception {
		when(jobOpeningService.list(eq(new JobOpeningFilter(null, null, null, null)), any()))
				.thenReturn(new PageImpl<>(List.of(), PageRequest.of(0, 10), 0));

		mockMvc.perform(get("/api/jobs"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.totalElements").value(0));
	}

	@Test
	void findByIdShouldReturnJobWithoutAuthentication() throws Exception {
		UUID jobId = UUID.fromString("00000000-0000-0000-0000-000000000010");
		when(jobOpeningService.findById(jobId)).thenReturn(new JobOpeningResponse(
				jobId,
				"Analista Java",
				"DescriÃ§Ã£o da vaga",
				"Spring Boot",
				"Tecnologia",
				"Remoto",
				JobStatus.OPEN,
				"Administrador RH",
				OffsetDateTime.parse("2026-08-05T10:00:00Z"),
				OffsetDateTime.parse("2026-08-05T10:00:00Z")));

		mockMvc.perform(get("/api/jobs/{id}", jobId))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(jobId.toString()))
				.andExpect(jsonPath("$.status").value("OPEN"));
	}

	@Test
	void listApplicationsShouldReturnUnauthorizedWithoutAuthentication() throws Exception {
		mockMvc.perform(get("/api/jobs/{id}/applications", UUID.fromString("00000000-0000-0000-0000-000000000010")))
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
				"Descrição da vaga",
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
				"Descrição da vaga",
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
		JobOpeningFilter filter = new JobOpeningFilter(JobStatus.OPEN, "java", "Tecnologia", "Remoto");
		when(jobOpeningService.list(eq(filter), any())).thenReturn(new PageImpl<>(List.of(new JobOpeningResponse(
				UUID.fromString("00000000-0000-0000-0000-000000000010"),
				"Analista Java",
				"Descrição da vaga",
				"Spring Boot",
				"Tecnologia",
				"Remoto",
				JobStatus.OPEN,
				"Administrador RH",
				OffsetDateTime.parse("2026-08-05T10:00:00Z"),
				OffsetDateTime.parse("2026-08-05T10:00:00Z"))), PageRequest.of(0, 10), 1));

		mockMvc.perform(get("/api/jobs")
						.param("status", "OPEN")
						.param("q", "java")
						.param("department", "Tecnologia")
						.param("location", "Remoto")
						.param("page", "0")
						.param("size", "10"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.content[0].id").value("00000000-0000-0000-0000-000000000010"))
				.andExpect(jsonPath("$.content[0].title").value("Analista Java"))
				.andExpect(jsonPath("$.content[0].status").value("OPEN"))
				.andExpect(jsonPath("$.totalElements").value(1))
				.andExpect(jsonPath("$.size").value(10))
				.andExpect(jsonPath("$.page").value(0))
				.andExpect(jsonPath("$.first").value(true))
				.andExpect(jsonPath("$.last").value(true));
	}

	@Test
	@WithMockUser(username = "admin", roles = "ADMIN")
	void listApplicationsShouldReturnPáginatedApplicationsForAdmin() throws Exception {
		UUID jobId = UUID.fromString("00000000-0000-0000-0000-000000000010");
		when(jobApplicationService.listByJob(eq(jobId), any())).thenReturn(new PageImpl<>(List.of(
				new JobApplicationResponse(
						UUID.fromString("00000000-0000-0000-0000-000000000020"),
						jobId,
						"Analista Java",
						UUID.fromString("00000000-0000-0000-0000-000000000030"),
						"Colaborador Teste",
						"Tecnologia",
						"Quero crescer na empresa.",
						ApplicationStatus.PENDING,
						null,
						OffsetDateTime.parse("2026-08-05T10:00:00Z"),
						OffsetDateTime.parse("2026-08-05T10:00:00Z"))),
				PageRequest.of(0, 10), 1));

		mockMvc.perform(get("/api/jobs/{id}/applications", jobId)
						.param("page", "0")
						.param("size", "10"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.content[0].id").value("00000000-0000-0000-0000-000000000020"))
				.andExpect(jsonPath("$.content[0].jobId").value(jobId.toString()))
				.andExpect(jsonPath("$.content[0].candidateName").value("Colaborador Teste"))
				.andExpect(jsonPath("$.content[0].status").value("PENDING"))
				.andExpect(jsonPath("$.totalElements").value(1))
				.andExpect(jsonPath("$.size").value(10))
				.andExpect(jsonPath("$.page").value(0))
				.andExpect(jsonPath("$.first").value(true))
				.andExpect(jsonPath("$.last").value(true));
	}

	private JobOpeningRequest validRequest() {
		return new JobOpeningRequest(
				"Analista Java",
				"Descrição da vaga",
				"Spring Boot",
				"Tecnologia",
				"Remoto",
				JobStatus.OPEN);
	}
}
