package br.com.abrahanarley.controllers;

import br.com.abrahanarley.dto.request.CandidateRegisterRequest;
import br.com.abrahanarley.dto.request.LoginRequest;
import br.com.abrahanarley.dto.response.AuthenticatedUserResponse;
import br.com.abrahanarley.dto.response.AuthResponse;
import br.com.abrahanarley.security.AuthenticatedUserService;
import br.com.abrahanarley.services.AuthService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController implements AuthApi {

	private final AuthService authService;
	private final AuthenticatedUserService authenticatedUserService;

	public AuthController(AuthService authService, AuthenticatedUserService authenticatedUserService) {
		this.authService = authService;
		this.authenticatedUserService = authenticatedUserService;
	}

	@Override
	public AuthResponse registerCandidate(CandidateRegisterRequest request) {
		return authService.registerCandidate(request);
	}

	@Override
	public AuthResponse login(LoginRequest request) {
		return authService.login(request);
	}

	@Override
	public AuthenticatedUserResponse me() {
		return AuthenticatedUserResponse.from(authenticatedUserService.currentUser());
	}
}
