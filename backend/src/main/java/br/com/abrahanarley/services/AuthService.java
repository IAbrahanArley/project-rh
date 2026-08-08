package br.com.abrahanarley.services;

import br.com.abrahanarley.dto.request.CandidateRegisterRequest;
import br.com.abrahanarley.dto.request.LoginRequest;
import br.com.abrahanarley.dto.response.AuthenticatedUserResponse;
import br.com.abrahanarley.dto.response.AuthResponse;
import br.com.abrahanarley.entities.AppUser;
import br.com.abrahanarley.enums.Role;
import br.com.abrahanarley.exceptions.BusinessException;
import br.com.abrahanarley.exceptions.ResourceNotFoundException;
import br.com.abrahanarley.repositories.UserRepository;
import br.com.abrahanarley.security.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class AuthService {

	private static final String CANDIDATE_DEPARTMENT = "Candidatos";

	private final AuthenticationManager authenticationManager;
	private final UserDetailsService userDetailsService;
	private final UserRepository userRepository;
	private final JwtService jwtService;
	private final PasswordEncoder passwordEncoder;

	public AuthService(AuthenticationManager authenticationManager, UserDetailsService userDetailsService,
			UserRepository userRepository, JwtService jwtService, PasswordEncoder passwordEncoder) {
		this.authenticationManager = authenticationManager;
		this.userDetailsService = userDetailsService;
		this.userRepository = userRepository;
		this.jwtService = jwtService;
		this.passwordEncoder = passwordEncoder;
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

	public AuthResponse registerCandidate(CandidateRegisterRequest request) {
		String username = normalizeUsername(request.username());
		String email = normalizeEmail(request.email());
		String fullName = normalizeFullName(request.fullName());

		if (userRepository.existsByUsernameIgnoreCase(username)) {
			throw new BusinessException("Username is already in use.");
		}

		if (userRepository.existsByEmailIgnoreCase(email)) {
			throw new BusinessException("Email is already in use.");
		}

		AppUser candidate = userRepository.save(new AppUser(
				username,
				passwordEncoder.encode(request.password()),
				fullName,
				email,
				CANDIDATE_DEPARTMENT,
				LocalDate.now(),
				Role.CANDIDATE));

		UserDetails userDetails = userDetailsService.loadUserByUsername(candidate.getUsername());
		String token = jwtService.generateToken(userDetails);

		return new AuthResponse("Bearer", token, jwtService.getExpirationSeconds(),
				AuthenticatedUserResponse.from(candidate));
	}

	private String normalizeUsername(String username) {
		return username.trim().toLowerCase();
	}

	private String normalizeEmail(String email) {
		return email.trim().toLowerCase();
	}

	private String normalizeFullName(String fullName) {
		return fullName.trim().replaceAll("\\s+", " ");
	}
}
