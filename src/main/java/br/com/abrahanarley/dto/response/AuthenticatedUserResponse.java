package br.com.abrahanarley.dto.response;

import br.com.abrahanarley.entities.AppUser;
import br.com.abrahanarley.enums.Role;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Dados basicos do usuario autenticado.")
public record AuthenticatedUserResponse(
		@Schema(description = "Identificador do usuario.", example = "1")
		Long id,
		@Schema(description = "Nome de usuario.", example = "admin")
		String username,
		@Schema(description = "Nome completo.", example = "Administrador RH")
		String fullName,
		@Schema(description = "Departamento do colaborador.", example = "Recursos Humanos")
		String department,
		@Schema(description = "Perfil de acesso.", example = "ADMIN")
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
