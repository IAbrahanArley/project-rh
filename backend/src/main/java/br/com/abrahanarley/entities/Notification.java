package br.com.abrahanarley.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "notifications")
public class Notification {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "recipient_id", nullable = false)
	private AppUser recipient;

	@Column(nullable = false, length = 140)
	private String subject;

	@Column(nullable = false, columnDefinition = "text")
	private String message;

	@Column(name = "read_flag", nullable = false)
	private boolean read = false;

	@Column(nullable = false, updatable = false)
	private OffsetDateTime createdAt;

	protected Notification() {
	}

	public Notification(AppUser recipient, String subject, String message) {
		this.recipient = recipient;
		this.subject = subject;
		this.message = message;
	}

	@PrePersist
	void prePersist() {
		createdAt = OffsetDateTime.now();
	}

	public void markAsRead() {
		read = true;
	}

	public UUID getId() {
		return id;
	}

	public AppUser getRecipient() {
		return recipient;
	}

	public String getSubject() {
		return subject;
	}

	public String getMessage() {
		return message;
	}

	public boolean isRead() {
		return read;
	}

	public OffsetDateTime getCreatedAt() {
		return createdAt;
	}
}
