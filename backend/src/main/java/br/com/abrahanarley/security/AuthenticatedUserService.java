package br.com.abrahanarley.security;

import br.com.abrahanarley.entities.AppUser;
import br.com.abrahanarley.exceptions.ResourceNotFoundException;
import br.com.abrahanarley.repositories.UserRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class AuthenticatedUserService {

	private final UserRepository userRepository;

	public AuthenticatedUserService(UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	public AppUser currentUser() {
		String username = SecurityContextHolder.getContext().getAuthentication().getName();
		return userRepository.findByUsername(username)
				.orElseThrow(() -> new ResourceNotFoundException("Authenticated user was not found."));
	}
}
