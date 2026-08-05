package br.com.abrahanarley.security;

import br.com.abrahanarley.repositories.UserRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class DatabaseUserDetailsService implements UserDetailsService {

	private final UserRepository userRepository;

	public DatabaseUserDetailsService(UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	@Override
	public UserDetails loadUserByUsername(String username) {
		return userRepository.findByUsername(username)
				.map(user -> User.withUsername(user.getUsername())
						.password(user.getPassword())
						.roles(user.getRole().name())
						.disabled(!user.isActive())
						.build())
				.orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
	}
}
