package br.com.abrahanarley.controllers;

import br.com.abrahanarley.config.SecurityConfig;
import br.com.abrahanarley.dto.request.ResumeUploadUrlRequest;
import br.com.abrahanarley.dto.response.ResumeDownloadUrlResponse;
import br.com.abrahanarley.dto.response.ResumeUploadUrlResponse;
import br.com.abrahanarley.exceptions.GlobalExceptionHandler;
import br.com.abrahanarley.security.JwtService;
import br.com.abrahanarley.services.CandidateResumeService;
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

import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = CandidateResumeController.class)
@Import({ SecurityConfig.class, GlobalExceptionHandler.class })
@TestPropertySource(properties = "app.cors.allowed-origins=http://localhost:4200")
class CandidateResumeControllerWebMvcTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockitoBean
	private CandidateResumeService candidateResumeService;

	@MockitoBean
	private JwtService jwtService;

	@MockitoBean
	private UserDetailsService userDetailsService;

	@Test
	void findMineShouldReturnUnauthorizedWithoutAuthentication() throws Exception {
		mockMvc.perform(get("/api/candidates/me/resume"))
				.andExpect(status().isUnauthorized());
	}

	@Test
	@WithMockUser(username = "colaborador", roles = "CANDIDATE")
	void findMineShouldReturnNoContentWhenResumeDoesNotExist() throws Exception {
		when(candidateResumeService.findMine()).thenReturn(Optional.empty());

		mockMvc.perform(get("/api/candidates/me/resume"))
				.andExpect(status().isNoContent());
	}

	@Test
	@WithMockUser(username = "colaborador", roles = "CANDIDATE")
	void createUploadUrlShouldReturnSignedUrlForCandidate() throws Exception {
		when(candidateResumeService.createUploadUrl(any())).thenReturn(new ResumeUploadUrlResponse(
				"https://bucket/upload",
				"resumes/candidate/resume.pdf",
				300,
				"application/pdf"));

		mockMvc.perform(post("/api/candidates/me/resume/upload-url")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(
								new ResumeUploadUrlRequest("currículo.pdf", "application/pdf", 1200))))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.uploadUrl").value("https://bucket/upload"))
				.andExpect(jsonPath("$.requiredContentType").value("application/pdf"));
	}

	@Test
	@WithMockUser(username = "admin", roles = "ADMIN")
	void createUploadUrlShouldReturnForbiddenForAdmin() throws Exception {
		mockMvc.perform(post("/api/candidates/me/resume/upload-url")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(
								new ResumeUploadUrlRequest("currículo.pdf", "application/pdf", 1200))))
				.andExpect(status().isForbidden());
	}

	@Test
	@WithMockUser(username = "admin", roles = "ADMIN")
	void createCandidateDownloadUrlShouldReturnSignedUrlForAdmin() throws Exception {
		UUID candidateId = UUID.fromString("00000000-0000-0000-0000-000000000002");
		when(candidateResumeService.createCandidateDownloadUrl(candidateId)).thenReturn(new ResumeDownloadUrlResponse(
				"https://bucket/download",
				300));

		mockMvc.perform(get("/api/candidates/{candidateId}/resume/download-url", candidateId))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.downloadUrl").value("https://bucket/download"));
	}
}
