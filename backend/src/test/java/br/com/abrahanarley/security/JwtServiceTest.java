package br.com.abrahanarley.security;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class JwtServiceTest {

	private static final String SECRET = "012345678901234567890123456789012345678901234567";

	@Test
	void generateTokenShouldKeepSubjectAndExpiration() {
		JwtService jwtService = new JwtService(SECRET, 120);
		User user = new User("admin", "password", List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));

		String token = jwtService.generateToken(user);

		assertThat(jwtService.extractUsername(token)).isEqualTo("admin");
		assertThat(jwtService.isTokenValid(token, user)).isTrue();
		assertThat(jwtService.getExpirationSeconds()).isEqualTo(7200);
	}

	@Test
	void tokenShouldBeInvalidForDifferentUser() {
		JwtService jwtService = new JwtService(SECRET, 120);
		User admin = new User("admin", "password", List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
		User candidate = new User("colaborador", "password", List.of(new SimpleGrantedAuthority("ROLE_CANDIDATE")));

		String token = jwtService.generateToken(admin);

		assertThat(jwtService.isTokenValid(token, candidate)).isFalse();
	}
}
