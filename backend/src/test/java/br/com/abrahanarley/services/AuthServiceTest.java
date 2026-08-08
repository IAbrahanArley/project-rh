package br.com.abrahanarley.services;

import br.com.abrahanarley.dto.request.CandidateRegisterRequest;
import br.com.abrahanarley.dto.request.LoginRequest;
import br.com.abrahanarley.dto.response.AuthResponse;
import br.com.abrahanarley.entities.AppUser;
import br.com.abrahanarley.enums.Role;
import br.com.abrahanarley.exceptions.BusinessException;
import br.com.abrahanarley.repositories.UserRepository;
import br.com.abrahanarley.security.JwtService;
import br.com.abrahanarley.support.TestFixtures;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

	@Mock
	private AuthenticationManager authenticationManager;

	@Mock
	private UserDetailsService userDetailsService;

	@Mock
	private UserRepository userRepository;

	@Mock
	private JwtService jwtService;

	@Mock
	private PasswordEncoder passwordEncoder;

	@InjectMocks
	private AuthService authService;

	@Test
	void loginShouldAuthenticateAndReturnJwtWithAuthenticatedUserSummary() {
		LoginRequest request = new LoginRequest("admin", "admin123");
		AppUser admin = TestFixtures.admin();
		UserDetails userDetails = User.withUsername("admin").password("admin123").roles("ADMIN").build();
		when(userDetailsService.loadUserByUsername("admin")).thenReturn(userDetails);
		when(userRepository.findByUsername("admin")).thenReturn(Optional.of(admin));
		when(jwtService.generateToken(userDetails)).thenReturn("jwt-token");
		when(jwtService.getExpirationSeconds()).thenReturn(7200L);

		AuthResponse response = authService.login(request);

		ArgumentCaptor<UsernamePasswordAuthenticationToken> authentication =
				ArgumentCaptor.forClass(UsernamePasswordAuthenticationToken.class);
		verify(authenticationManager).authenticate(authentication.capture());
		assertThat(authentication.getValue().getPrincipal()).isEqualTo("admin");
		assertThat(authentication.getValue().getCredentials()).isEqualTo("admin123");
		assertThat(response.tokenType()).isEqualTo("Bearer");
		assertThat(response.accessToken()).isEqualTo("jwt-token");
		assertThat(response.expiresInSeconds()).isEqualTo(7200L);
		assertThat(response.user().username()).isEqualTo("admin");
		assertThat(response.user().fullName()).isEqualTo("Administrador RH");
	}

	@Test
	void registerCandidateShouldCreateCandidateAndReturnJwt() {
		CandidateRegisterRequest request = new CandidateRegisterRequest(
				" Maria.Silva ",
				" Maria  Silva ",
				" MARIA.SILVA@EMAIL.COM ",
				"Senha123");
		UserDetails userDetails = User.withUsername("maria.silva").password("encoded-password").roles("CANDIDATE")
				.build();
		when(userRepository.existsByUsernameIgnoreCase("maria.silva")).thenReturn(false);
		when(userRepository.existsByEmailIgnoreCase("maria.silva@email.com")).thenReturn(false);
		when(passwordEncoder.encode("Senha123")).thenReturn("encoded-password");
		when(userRepository.save(any(AppUser.class))).thenAnswer(invocation -> {
			AppUser candidate = invocation.getArgument(0);
			ReflectionTestUtils.setField(candidate, "id", TestFixtures.CANDIDATE_ID);
			return candidate;
		});
		when(userDetailsService.loadUserByUsername("maria.silva")).thenReturn(userDetails);
		when(jwtService.generateToken(userDetails)).thenReturn("candidate-token");
		when(jwtService.getExpirationSeconds()).thenReturn(7200L);

		AuthResponse response = authService.registerCandidate(request);

		assertThat(response.accessToken()).isEqualTo("candidate-token");
		assertThat(response.user().username()).isEqualTo("maria.silva");
		assertThat(response.user().fullName()).isEqualTo("Maria Silva");
		assertThat(response.user().email()).isEqualTo("maria.silva@email.com");
		assertThat(response.user().department()).isEqualTo("Candidatos");
		assertThat(response.user().role()).isEqualTo(Role.CANDIDATE);
	}

	@Test
	void registerCandidateShouldRejectDuplicateUsername() {
		CandidateRegisterRequest request = new CandidateRegisterRequest(
				"colaborador",
				"Colaborador Teste",
				"novo@email.com",
				"Senha123");
		when(userRepository.existsByUsernameIgnoreCase("colaborador")).thenReturn(true);

		assertThatThrownBy(() -> authService.registerCandidate(request))
				.isInstanceOf(BusinessException.class)
				.hasMessage("Username is already in use.");
		verify(userRepository, never()).save(any(AppUser.class));
	}

	@Test
	void registerCandidateShouldRejectDuplicateEmail() {
		CandidateRegisterRequest request = new CandidateRegisterRequest(
				"novo",
				"Novo Candidato",
				"colaborador@empresa.com",
				"Senha123");
		when(userRepository.existsByUsernameIgnoreCase("novo")).thenReturn(false);
		when(userRepository.existsByEmailIgnoreCase("colaborador@empresa.com")).thenReturn(true);

		assertThatThrownBy(() -> authService.registerCandidate(request))
				.isInstanceOf(BusinessException.class)
				.hasMessage("Email is already in use.");
		verify(userRepository, never()).save(any(AppUser.class));
	}
}
