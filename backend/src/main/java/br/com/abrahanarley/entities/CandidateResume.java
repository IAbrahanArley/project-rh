package br.com.abrahanarley.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "candidate_resumes")
public class CandidateResume {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@OneToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "candidate_id", nullable = false, unique = true)
	private AppUser candidate;

	@Column(nullable = false, length = 180)
	private String fileName;

	@Column(nullable = false, length = 80)
	private String contentType;

	@Column(nullable = false)
	private long sizeBytes;

	@Column(nullable = false, unique = true, length = 500)
	private String storageKey;

	@Column(nullable = false)
	private OffsetDateTime uploadedAt;

	protected CandidateResume() {
	}

	public CandidateResume(AppUser candidate, String fileName, String contentType, long sizeBytes, String storageKey) {
		this.candidate = candidate;
		update(fileName, contentType, sizeBytes, storageKey);
	}

	public void update(String fileName, String contentType, long sizeBytes, String storageKey) {
		this.fileName = fileName;
		this.contentType = contentType;
		this.sizeBytes = sizeBytes;
		this.storageKey = storageKey;
	}

	@PrePersist
	@PreUpdate
	void touchUploadedAt() {
		uploadedAt = OffsetDateTime.now();
	}

	public UUID getId() {
		return id;
	}

	public AppUser getCandidate() {
		return candidate;
	}

	public String getFileName() {
		return fileName;
	}

	public String getContentType() {
		return contentType;
	}

	public long getSizeBytes() {
		return sizeBytes;
	}

	public String getStorageKey() {
		return storageKey;
	}

	public OffsetDateTime getUploadedAt() {
		return uploadedAt;
	}
}
