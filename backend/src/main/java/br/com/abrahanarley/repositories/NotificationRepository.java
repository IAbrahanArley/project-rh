package br.com.abrahanarley.repositories;

import br.com.abrahanarley.entities.AppUser;
import br.com.abrahanarley.entities.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface NotificationRepository extends JpaRepository<Notification, UUID> {

	Page<Notification> findByRecipient(AppUser recipient, Pageable pageable);

	Page<Notification> findByRecipientAndRead(AppUser recipient, boolean read, Pageable pageable);

	long countByRecipientAndReadFalse(AppUser recipient);
}
