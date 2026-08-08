package br.com.abrahanarley.services;

import br.com.abrahanarley.dto.response.NotificationResponse;
import br.com.abrahanarley.dto.response.UnreadNotificationsResponse;
import br.com.abrahanarley.entities.AppUser;
import br.com.abrahanarley.entities.Notification;
import br.com.abrahanarley.exceptions.ResourceNotFoundException;
import br.com.abrahanarley.repositories.NotificationRepository;
import br.com.abrahanarley.security.AuthenticatedUserService;
import br.com.abrahanarley.support.TestFixtures;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

	@Mock
	private NotificationRepository notificationRepository;

	@Mock
	private AuthenticatedUserService authenticatedUserService;

	@InjectMocks
	private NotificationService notificationService;

	@Test
	void listMineShouldReturnPagedNotificationsWithoutReadFilter() {
		AppUser candidate = TestFixtures.candidate();
		Notification notification = TestFixtures.notification(TestFixtures.NOTIFICATION_ID, candidate);
		PageRequest pageable = PageRequest.of(0, 10);
		when(authenticatedUserService.currentUser()).thenReturn(candidate);
		when(notificationRepository.findByRecipient(candidate, pageable))
				.thenReturn(new PageImpl<>(List.of(notification), pageable, 1));

		Page<NotificationResponse> response = notificationService.listMine(null, pageable);

		assertThat(response.getTotalElements()).isEqualTo(1);
		assertThat(response.getContent().getFirst().id()).isEqualTo(TestFixtures.NOTIFICATION_ID);
	}

	@Test
	void listMineShouldFilterByReadStatus() {
		AppUser candidate = TestFixtures.candidate();
		PageRequest pageable = PageRequest.of(0, 10);
		when(authenticatedUserService.currentUser()).thenReturn(candidate);
		when(notificationRepository.findByRecipientAndRead(candidate, false, pageable))
				.thenReturn(Page.empty(pageable));

		Page<NotificationResponse> response = notificationService.listMine(false, pageable);

		assertThat(response.getTotalElements()).isZero();
	}

	@Test
	void countMineUnreadShouldReturnUnreadCount() {
		AppUser candidate = TestFixtures.candidate();
		when(authenticatedUserService.currentUser()).thenReturn(candidate);
		when(notificationRepository.countByRecipientAndReadFalse(candidate)).thenReturn(3L);

		UnreadNotificationsResponse response = notificationService.countMineUnread();

		assertThat(response.unreadCount()).isEqualTo(3L);
	}

	@Test
	void markMineAsReadShouldMarkNotificationWhenCurrentUserIsRecipient() {
		AppUser candidate = TestFixtures.candidate();
		Notification notification = TestFixtures.notification(TestFixtures.NOTIFICATION_ID, candidate);
		when(authenticatedUserService.currentUser()).thenReturn(candidate);
		when(notificationRepository.findById(TestFixtures.NOTIFICATION_ID)).thenReturn(Optional.of(notification));

		NotificationResponse response = notificationService.markMineAsRead(TestFixtures.NOTIFICATION_ID);

		assertThat(response.id()).isEqualTo(TestFixtures.NOTIFICATION_ID);
		assertThat(response.read()).isTrue();
		assertThat(notification.isRead()).isTrue();
	}

	@Test
	void markMineAsReadShouldHideNotificationsFromOtherUsers() {
		AppUser currentUser = TestFixtures.candidate();
		AppUser otherUser = TestFixtures.user(TestFixtures.OTHER_USER_ID, "outro", "Outro Usuario",
				currentUser.getRole());
		Notification notification = TestFixtures.notification(TestFixtures.OTHER_NOTIFICATION_ID, otherUser);
		when(authenticatedUserService.currentUser()).thenReturn(currentUser);
		when(notificationRepository.findById(TestFixtures.OTHER_NOTIFICATION_ID)).thenReturn(Optional.of(notification));

		assertThatThrownBy(() -> notificationService.markMineAsRead(TestFixtures.OTHER_NOTIFICATION_ID))
				.isInstanceOf(ResourceNotFoundException.class)
				.hasMessage("Notification not found.");

		assertThat(notification.isRead()).isFalse();
	}

	@Test
	void markAllMineAsReadShouldMarkUnreadNotifications() {
		AppUser candidate = TestFixtures.candidate();
		Notification firstNotification = TestFixtures.notification(TestFixtures.NOTIFICATION_ID, candidate);
		Notification secondNotification = TestFixtures.notification(TestFixtures.OTHER_NOTIFICATION_ID, candidate);
		when(authenticatedUserService.currentUser()).thenReturn(candidate);
		when(notificationRepository.findByRecipientAndRead(eq(candidate), eq(false), any(Pageable.class)))
				.thenReturn(new PageImpl<>(List.of(firstNotification, secondNotification)));

		UnreadNotificationsResponse response = notificationService.markAllMineAsRead();

		assertThat(response.unreadCount()).isZero();
		assertThat(firstNotification.isRead()).isTrue();
		assertThat(secondNotification.isRead()).isTrue();
		verify(notificationRepository).findByRecipientAndRead(eq(candidate), eq(false), any(Pageable.class));
	}
}
