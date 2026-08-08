package br.com.abrahanarley.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.data.domain.Page;

import java.util.List;

@Schema(description = "Resposta paginada.")
public record PageResponse<T>(
		@Schema(description = "Itens da pagina atual.")
		List<T> content,
		@Schema(description = "Numero da pagina atual, iniciando em zero.", example = "0")
		int page,
		@Schema(description = "Quantidade de itens solicitada por pagina.", example = "10")
		int size,
		@Schema(description = "Quantidade total de itens encontrados.", example = "42")
		long totalElements,
		@Schema(description = "Quantidade total de paginas.", example = "5")
		int totalPages,
		@Schema(description = "Indica se esta e a primeira pagina.", example = "true")
		boolean first,
		@Schema(description = "Indica se esta e a ultima pagina.", example = "false")
		boolean last) {

	public static <T> PageResponse<T> from(Page<T> page) {
		return new PageResponse<>(
				page.getContent(),
				page.getNumber(),
				page.getSize(),
				page.getTotalElements(),
				page.getTotalPages(),
				page.isFirst(),
				page.isLast());
	}
}
