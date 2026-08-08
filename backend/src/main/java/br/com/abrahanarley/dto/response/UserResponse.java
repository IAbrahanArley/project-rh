package br.com.abrahanarley.dto.response;

import br.com.abrahanarley.entities.AppUser;
import br.com.abrahanarley.enums.Role;

import java.time.LocalDate;
import java.util.UUID;

public record UserResponse(
		UUID id,
		String username,
		String fullName,
		String email,
		String department,
		LocalDate hireDate,
		Role role) {

	public static UserResponse from(AppUser user) {
		return new UserResponse(
				user.getId(),
				user.getUsername(),
				user.getFullName(),
				user.getEmail(),
				user.getDepartment(),
				user.getHireDate(),
				user.getRole());
	}
}
