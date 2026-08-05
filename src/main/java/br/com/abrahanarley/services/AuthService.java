package br.com.abrahanarley.services;

import br.com.abrahanarley.dto.request.LoginRequest;
import br.com.abrahanarley.dto.response.AuthenticatedUserResponse;
import br.com.abrahanarley.dto.response.AuthResponse;
import br.com.abrahanarley.entities.AppUser;
import br.com.abrahanarley.exceptions.ResourceNotFoundException;
import br.com.abrahanarley.repositories.UserRepository;
import br.com.abrahanarley.security.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

	private final AuthenticationManager authenticationManager;
	private final UserDetailsService userDetailsService;
	private final UserRepository userRepository;
	private final JwtService jwtService;

	public AuthService(AuthenticationManager authenticationManager, UserDetailsService userDetailsService,
			UserRepository userRepository, JwtService jwtService) {
		this.authenticationManager = authenticationManager;
		this.userDetailsService = userDetailsService;
		this.userRepository = userRepository;
		this.jwtService = jwtService;
	}

	public AuthResponse login(LoginRequest request) {
		authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
				request.username(),
				request.password()));

		UserDetails userDetails = userDetailsService.loadUserByUsername(request.username());
		AppUser user = userRepository.findByUsername(request.username())
				.orElseThrow(() -> new ResourceNotFoundException("User not found."));
		String token = jwtService.generateToken(userDetails);

		return new AuthResponse("Bearer", token, jwtService.getExpirationSeconds(), AuthenticatedUserResponse.from(user));
	}
}
