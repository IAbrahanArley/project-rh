package br.com.abrahanarley.dto.response;

import br.com.abrahanarley.entities.AppUser;
import br.com.abrahanarley.enums.Role;

public record AuthenticatedUserResponse(
		Long id,
		String username,
		String fullName,
		String department,
		Role role) {

	public static AuthenticatedUserResponse from(AppUser user) {
		return new AuthenticatedUserResponse(
				user.getId(),
				user.getUsername(),
				user.getFullName(),
				user.getDepartment(),
				user.getRole());
	}
}
