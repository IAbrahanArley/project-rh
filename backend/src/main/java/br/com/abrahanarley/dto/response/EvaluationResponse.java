package br.com.abrahanarley.dto.response;

import br.com.abrahanarley.entities.CandidateEvaluation;

import java.time.OffsetDateTime;
import java.util.UUID;

public record EvaluationResponse(
		UUID id,
		UUID applicationId,
		String evaluatorName,
		Integer score,
		boolean recommended,
		String comments,
		OffsetDateTime createdAt,
		OffsetDateTime updatedAt) {

	public static EvaluationResponse from(CandidateEvaluation evaluation) {
		return new EvaluationResponse(
				evaluation.getId(),
				evaluation.getApplication().getId(),
				evaluation.getEvaluator().getFullName(),
				evaluation.getScore(),
				evaluation.isRecommended(),
				evaluation.getComments(),
				evaluation.getCreatedAt(),
				evaluation.getUpdatedAt());
	}
}
