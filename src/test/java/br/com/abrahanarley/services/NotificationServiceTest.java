package br.com.abrahanarley.services;

import br.com.abrahanarley.dto.response.NotificationResponse;
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

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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
	void markMineAsReadShouldMarkNotificationWhenCurrentUserIsRecipient() {
		AppUser candidate = TestFixtures.candidate();
		Notification notification = TestFixtures.notification(40L, candidate);
		when(authenticatedUserService.currentUser()).thenReturn(candidate);
		when(notificationRepository.findById(40L)).thenReturn(Optional.of(notification));

		NotificationResponse response = notificationService.markMineAsRead(40L);

		assertThat(response.id()).isEqualTo(40L);
		assertThat(response.read()).isTrue();
		assertThat(notification.isRead()).isTrue();
	}

	@Test
	void markMineAsReadShouldHideNotificationsFromOtherUsers() {
		AppUser currentUser = TestFixtures.candidate();
		AppUser otherUser = TestFixtures.user(3L, "outro", "Outro Usuario", currentUser.getRole());
		Notification notification = TestFixtures.notification(41L, otherUser);
		when(authenticatedUserService.currentUser()).thenReturn(currentUser);
		when(notificationRepository.findById(41L)).thenReturn(Optional.of(notification));

		assertThatThrownBy(() -> notificationService.markMineAsRead(41L))
				.isInstanceOf(ResourceNotFoundException.class)
				.hasMessage("Notification not found.");

		assertThat(notification.isRead()).isFalse();
	}
}
