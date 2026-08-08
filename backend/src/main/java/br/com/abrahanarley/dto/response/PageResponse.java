package br.com.abrahanarley.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.data.domain.Page;

import java.util.List;

@Schema(description = "Resposta páginada.")
public record PageResponse<T>(
		@Schema(description = "Itens da página atual.")
		List<T> content,
		@Schema(description = "Numero da página atual, iniciando em zero.", example = "0")
		int page,
		@Schema(description = "Quantidade de itens solicitada por página.", example = "10")
		int size,
		@Schema(description = "Quantidade total de itens encontrados.", example = "42")
		long totalElements,
		@Schema(description = "Quantidade total de páginas.", example = "5")
		int totalPages,
		@Schema(description = "Indica se esta e a primeira página.", example = "true")
		boolean first,
		@Schema(description = "Indica se esta e a última página.", example = "false")
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
