package br.com.abrahanarley.entities;

import br.com.abrahanarley.enums.JobStatus;
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

import java.time.OffsetDateTime;

@Entity
@Table(name = "job_openings")
public class JobOpening {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, length = 140)
	private String title;

	@Column(nullable = false, columnDefinition = "text")
	private String description;

	@Column(nullable = false, columnDefinition = "text")
	private String requirements;

	@Column(nullable = false, length = 80)
	private String department;

	@Column(nullable = false, length = 80)
	private String location;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 30)
	private JobStatus status = JobStatus.OPEN;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "created_by_id", nullable = false)
	private AppUser createdBy;

	@Column(nullable = false, updatable = false)
	private OffsetDateTime createdAt;

	@Column(nullable = false)
	private OffsetDateTime updatedAt;

	protected JobOpening() {
	}

	public JobOpening(String title, String description, String requirements, String department, String location,
			AppUser createdBy) {
		this.title = title;
		this.description = description;
		this.requirements = requirements;
		this.department = department;
		this.location = location;
		this.createdBy = createdBy;
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

	public void update(String title, String description, String requirements, String department, String location,
			JobStatus status) {
		this.title = title;
		this.description = description;
		this.requirements = requirements;
		this.department = department;
		this.location = location;
		this.status = status;
	}

	public void cancel() {
		status = JobStatus.CANCELLED;
	}

	public Long getId() {
		return id;
	}

	public String getTitle() {
		return title;
	}

	public String getDescription() {
		return description;
	}

	public String getRequirements() {
		return requirements;
	}

	public String getDepartment() {
		return department;
	}

	public String getLocation() {
		return location;
	}

	public JobStatus getStatus() {
		return status;
	}

	public AppUser getCreatedBy() {
		return createdBy;
	}

	public OffsetDateTime getCreatedAt() {
		return createdAt;
	}

	public OffsetDateTime getUpdatedAt() {
		return updatedAt;
	}
}
