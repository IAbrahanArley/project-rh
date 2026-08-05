package br.com.abrahanarley.controllers;

import br.com.abrahanarley.dto.request.LoginRequest;
import br.com.abrahanarley.dto.response.AuthenticatedUserResponse;
import br.com.abrahanarley.dto.response.AuthResponse;
import br.com.abrahanarley.security.AuthenticatedUserService;
import br.com.abrahanarley.services.AuthService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

	private final AuthService authService;
	private final AuthenticatedUserService authenticatedUserService;

	public AuthController(AuthService authService, AuthenticatedUserService authenticatedUserService) {
		this.authService = authService;
		this.authenticatedUserService = authenticatedUserService;
	}

	@PostMapping("/login")
	public AuthResponse login(@RequestBody @Valid LoginRequest request) {
		return authService.login(request);
	}

	@GetMapping("/me")
	public AuthenticatedUserResponse me() {
		return AuthenticatedUserResponse.from(authenticatedUserService.currentUser());
	}
}
