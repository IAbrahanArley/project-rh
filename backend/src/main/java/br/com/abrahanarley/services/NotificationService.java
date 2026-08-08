package br.com.abrahanarley.services;

import br.com.abrahanarley.dto.response.NotificationResponse;
import br.com.abrahanarley.dto.response.UnreadNotificationsResponse;
import br.com.abrahanarley.entities.AppUser;
import br.com.abrahanarley.entities.Notification;
import br.com.abrahanarley.exceptions.ResourceNotFoundException;
import br.com.abrahanarley.repositories.NotificationRepository;
import br.com.abrahanarley.security.AuthenticatedUserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class NotificationService {

	private final NotificationRepository notificationRepository;
	private final AuthenticatedUserService authenticatedUserService;

	public NotificationService(NotificationRepository notificationRepository,
			AuthenticatedUserService authenticatedUserService) {
		this.notificationRepository = notificationRepository;
		this.authenticatedUserService = authenticatedUserService;
	}

	@Transactional
	public void notify(AppUser recipient, String subject, String message) {
		notificationRepository.save(new Notification(recipient, subject, message));
	}

	@Transactional(readOnly = true)
	public Page<NotificationResponse> listMine(Boolean read, Pageable pageable) {
		AppUser currentUser = authenticatedUserService.currentUser();
		Page<Notification> notifications = read == null
				? notificationRepository.findByRecipient(currentUser, pageable)
				: notificationRepository.findByRecipientAndRead(currentUser, read, pageable);

		return notifications.map(NotificationResponse::from);
	}

	@Transactional(readOnly = true)
	public UnreadNotificationsResponse countMineUnread() {
		AppUser currentUser = authenticatedUserService.currentUser();
		return new UnreadNotificationsResponse(notificationRepository.countByRecipientAndReadFalse(currentUser));
	}

	@Transactional
	public NotificationResponse markMineAsRead(UUID id) {
		AppUser currentUser = authenticatedUserService.currentUser();
		Notification notification = notificationRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Notification not found."));

		if (!notification.getRecipient().getId().equals(currentUser.getId())) {
			throw new ResourceNotFoundException("Notification not found.");
		}

		notification.markAsRead();
		return NotificationResponse.from(notification);
	}

	@Transactional
	public UnreadNotificationsResponse markAllMineAsRead() {
		AppUser currentUser = authenticatedUserService.currentUser();
		Page<Notification> unreadNotifications = notificationRepository.findByRecipientAndRead(
				currentUser,
				false,
				Pageable.unpaged());

		unreadNotifications.forEach(Notification::markAsRead);

		return new UnreadNotificationsResponse(0);
	}
}
