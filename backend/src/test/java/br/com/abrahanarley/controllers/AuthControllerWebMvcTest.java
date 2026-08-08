package br.com.abrahanarley.controllers;

import br.com.abrahanarley.dto.request.CandidateRegisterRequest;
import br.com.abrahanarley.config.SecurityConfig;
import br.com.abrahanarley.dto.request.LoginRequest;
import br.com.abrahanarley.dto.response.AuthResponse;
import br.com.abrahanarley.dto.response.AuthenticatedUserResponse;
import br.com.abrahanarley.entities.AppUser;
import br.com.abrahanarley.exceptions.GlobalExceptionHandler;
import br.com.abrahanarley.security.AuthenticatedUserService;
import br.com.abrahanarley.security.JwtService;
import br.com.abrahanarley.services.AuthService;
import br.com.abrahanarley.support.TestFixtures;
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

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@Import({ SecurityConfig.class, GlobalExceptionHandler.class })
@TestPropertySource(properties = "app.cors.allowed-origins=http://localhost:4200")
class AuthControllerWebMvcTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockitoBean
	private AuthService authService;

	@MockitoBean
	private AuthenticatedUserService authenticatedUserService;

	@MockitoBean
	private JwtService jwtService;

	@MockitoBean
	private UserDetailsService userDetailsService;

	@Test
	void registerCandidateShouldReturnTokenAndAuthenticatedUser() throws Exception {
		AppUser candidate = TestFixtures.candidate();
		CandidateRegisterRequest request = new CandidateRegisterRequest(
				"colaborador",
				"Colaborador Teste",
				"colaborador@empresa.com",
				"user12345");
		when(authService.registerCandidate(request)).thenReturn(new AuthResponse(
				"Bearer",
				"candidate-token",
				7200,
				AuthenticatedUserResponse.from(candidate)));

		mockMvc.perform(post("/api/auth/candidate/register")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.tokenType").value("Bearer"))
				.andExpect(jsonPath("$.accessToken").value("candidate-token"))
				.andExpect(jsonPath("$.user.username").value("colaborador"))
				.andExpect(jsonPath("$.user.role").value("CANDIDATE"));
	}

	@Test
	void registerCandidateShouldReturnBadRequestWhenPayloadIsInvalid() throws Exception {
		CandidateRegisterRequest request = new CandidateRegisterRequest(
				"@@",
				"",
				"email-inválido",
				"curta");

		mockMvc.perform(post("/api/auth/candidate/register")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.status").value(400))
				.andExpect(jsonPath("$.path").value("/api/auth/candidate/register"));
	}

	@Test
	void loginShouldReturnTokenAndAuthenticatedUser() throws Exception {
		AppUser admin = TestFixtures.admin();
		LoginRequest request = new LoginRequest("admin", "admin123");
		when(authService.login(request)).thenReturn(new AuthResponse(
				"Bearer",
				"jwt-token",
				7200,
				AuthenticatedUserResponse.from(admin)));

		mockMvc.perform(post("/api/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.tokenType").value("Bearer"))
				.andExpect(jsonPath("$.accessToken").value("jwt-token"))
				.andExpect(jsonPath("$.expiresInSeconds").value(7200))
				.andExpect(jsonPath("$.user.username").value("admin"))
				.andExpect(jsonPath("$.user.fullName").value("Administrador RH"));
	}

	@Test
	void loginShouldReturnBadRequestWhenPayloadIsInvalid() throws Exception {
		LoginRequest request = new LoginRequest("", "");

		mockMvc.perform(post("/api/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.status").value(400))
				.andExpect(jsonPath("$.path").value("/api/auth/login"));
	}

	@Test
	@WithMockUser(username = "admin", roles = "ADMIN")
	void meShouldReturnCurrentAuthenticatedUser() throws Exception {
		when(authenticatedUserService.currentUser()).thenReturn(TestFixtures.admin());

		mockMvc.perform(get("/api/auth/me"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(TestFixtures.ADMIN_ID.toString()))
				.andExpect(jsonPath("$.username").value("admin"))
				.andExpect(jsonPath("$.role").value("ADMIN"));
	}

	@Test
	void meShouldReturnUnauthorizedWithoutAuthentication() throws Exception {
		mockMvc.perform(get("/api/auth/me"))
				.andExpect(status().isUnauthorized());
	}
}
