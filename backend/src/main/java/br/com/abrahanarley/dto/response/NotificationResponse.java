package br.com.abrahanarley.dto.response;

import br.com.abrahanarley.entities.Notification;

import java.time.OffsetDateTime;
import java.util.UUID;

public record NotificationResponse(
		UUID id,
		String subject,
		String message,
		boolean read,
		OffsetDateTime createdAt) {

	public static NotificationResponse from(Notification notification) {
		return new NotificationResponse(
				notification.getId(),
				notification.getSubject(),
				notification.getMessage(),
				notification.isRead(),
				notification.getCreatedAt());
	}
}
