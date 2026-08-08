package br.com.abrahanarley.dto.response;

import br.com.abrahanarley.entities.JobApplication;
import br.com.abrahanarley.enums.ApplicationStatus;

import java.time.OffsetDateTime;
import java.util.UUID;

public record JobApplicationResponse(
		UUID id,
		UUID jobId,
		String jobTitle,
		UUID candidateId,
		String candidateName,
		String candidateDepartment,
		String motivation,
		ApplicationStatus status,
		String feedback,
		OffsetDateTime createdAt,
		OffsetDateTime updatedAt) {

	public static JobApplicationResponse from(JobApplication application) {
		return new JobApplicationResponse(
				application.getId(),
				application.getJobOpening().getId(),
				application.getJobOpening().getTitle(),
				application.getCandidate().getId(),
				application.getCandidate().getFullName(),
				application.getCandidate().getDepartment(),
				application.getMotivation(),
				application.getStatus(),
				application.getFeedback(),
				application.getCreatedAt(),
				application.getUpdatedAt());
	}
}
