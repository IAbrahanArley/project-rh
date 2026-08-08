package br.com.abrahanarley.controllers;

import br.com.abrahanarley.dto.response.NotificationResponse;
import br.com.abrahanarley.dto.response.PageResponse;
import br.com.abrahanarley.dto.response.UnreadNotificationsResponse;
import br.com.abrahanarley.services.NotificationService;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.SortDefault;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController implements NotificationApi {

	private final NotificationService notificationService;

	public NotificationController(NotificationService notificationService) {
		this.notificationService = notificationService;
	}

	@Override
	public PageResponse<NotificationResponse> listMine(
			@RequestParam(required = false) Boolean read,
			@PageableDefault(size = 10)
			@SortDefault(sort = "createdAt", direction = org.springframework.data.domain.Sort.Direction.DESC)
			Pageable pageable) {
		return PageResponse.from(notificationService.listMine(read, pageable));
	}

	@Override
	public UnreadNotificationsResponse countMineUnread() {
		return notificationService.countMineUnread();
	}

	@Override
	public NotificationResponse markAsRead(@PathVariable UUID id) {
		return notificationService.markMineAsRead(id);
	}

	@Override
	public UnreadNotificationsResponse markAllAsRead() {
		return notificationService.markAllMineAsRead();
	}
}
