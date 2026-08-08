package br.com.abrahanarley.services;

import br.com.abrahanarley.dto.request.LoginRequest;
import br.com.abrahanarley.dto.response.AuthResponse;
import br.com.abrahanarley.entities.AppUser;
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

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
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
}
