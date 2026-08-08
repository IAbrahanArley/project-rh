package br.com.abrahanarley.entities;

import br.com.abrahanarley.enums.ApplicationStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "job_applications", uniqueConstraints = {
		@UniqueConstraint(name = "uk_job_applications_candidate_job", columnNames = { "candidate_id", "job_opening_id" })
})
public class JobApplication {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "candidate_id", nullable = false)
	private AppUser candidate;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "job_opening_id", nullable = false)
	private JobOpening jobOpening;

	@Column(nullable = false, columnDefinition = "text")
	private String motivation;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 30)
	private ApplicationStatus status = ApplicationStatus.PENDING;

	@Column(columnDefinition = "text")
	private String feedback;

	@Column(nullable = false, updatable = false)
	private OffsetDateTime createdAt;

	@Column(nullable = false)
	private OffsetDateTime updatedAt;

	protected JobApplication() {
	}

	public JobApplication(AppUser candidate, JobOpening jobOpening, String motivation) {
		this.candidate = candidate;
		this.jobOpening = jobOpening;
		this.motivation = motivation;
	}

	@PrePersist
	void prePersist() {
		createdAt = OffsetDateTime.now();
		updatedAt = createdAt;
	}

	@PreUpdate
	void preUpdate() {
		updatedAt = OffsetDateTime.now();
	}

	public void updateStatus(ApplicationStatus status, String feedback) {
		this.status = status;
		this.feedback = feedback;
	}

	public UUID getId() {
		return id;
	}

	public AppUser getCandidate() {
		return candidate;
	}

	public JobOpening getJobOpening() {
		return jobOpening;
	}

	public String getMotivation() {
		return motivation;
	}

	public ApplicationStatus getStatus() {
		return status;
	}

	public String getFeedback() {
		return feedback;
	}

	public OffsetDateTime getCreatedAt() {
		return createdAt;
	}

	public OffsetDateTime getUpdatedAt() {
		return updatedAt;
	}
}
