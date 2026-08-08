package br.com.abrahanarley.dto.response;

import br.com.abrahanarley.entities.AppUser;
import br.com.abrahanarley.enums.Role;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(description = "Dados básicos do usuário autenticado.")
public record AuthenticatedUserResponse(
		@Schema(description = "Identificador público do usuário.", example = "7f36b6d7-2f3d-4bb1-8d83-598a04fb9b4c")
		UUID id,
		@Schema(description = "Nome de usuário.", example = "admin")
		String username,
		@Schema(description = "Nome completo.", example = "Administrador RH")
		String fullName,
		@Schema(description = "E-mail do usuário.", example = "admin@empresa.com")
		String email,
		@Schema(description = "Departamento do colaborador.", example = "Recursos Humanos")
		String department,
		@Schema(description = "Perfil de acesso.", example = "ADMIN")
		Role role) {

	public static AuthenticatedUserResponse from(AppUser user) {
		return new AuthenticatedUserResponse(
				user.getId(),
				user.getUsername(),
				user.getFullName(),
				user.getEmail(),
				user.getDepartment(),
				user.getRole());
	}
}
