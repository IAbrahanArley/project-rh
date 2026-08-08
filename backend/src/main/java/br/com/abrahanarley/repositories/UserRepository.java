package br.com.abrahanarley.repositories;

import br.com.abrahanarley.entities.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<AppUser, UUID> {

	Optional<AppUser> findByUsername(String username);

	boolean existsByUsername(String username);

	boolean existsByUsernameIgnoreCase(String username);

	boolean existsByEmailIgnoreCase(String email);
}
