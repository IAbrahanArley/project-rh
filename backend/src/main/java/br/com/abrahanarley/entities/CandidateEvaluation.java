package br.com.abrahanarley.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "candidate_evaluations")
public class CandidateEvaluation {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "application_id", nullable = false, unique = true)
	private JobApplication application;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "evaluator_id", nullable = false)
	private AppUser evaluator;

	@Column(nullable = false)
	private Integer score;

	@Column(nullable = false)
	private boolean recommended;

	@Column(nullable = false, columnDefinition = "text")
	private String comments;

	@Column(nullable = false, updatable = false)
	private OffsetDateTime createdAt;

	@Column(nullable = false)
	private OffsetDateTime updatedAt;

	protected CandidateEvaluation() {
	}

	public CandidateEvaluation(JobApplication application, AppUser evaluator, Integer score, boolean recommended,
			String comments) {
		this.application = application;
		this.evaluator = evaluator;
		this.score = score;
		this.recommended = recommended;
		this.comments = comments;
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

	public void update(AppUser evaluator, Integer score, boolean recommended, String comments) {
		this.evaluator = evaluator;
		this.score = score;
		this.recommended = recommended;
		this.comments = comments;
	}

	public UUID getId() {
		return id;
	}

	public JobApplication getApplication() {
		return application;
	}

	public AppUser getEvaluator() {
		return evaluator;
	}

	public Integer getScore() {
		return score;
	}

	public boolean isRecommended() {
		return recommended;
	}

	public String getComments() {
		return comments;
	}

	public OffsetDateTime getCreatedAt() {
		return createdAt;
	}

	public OffsetDateTime getUpdatedAt() {
		return updatedAt;
	}
}
