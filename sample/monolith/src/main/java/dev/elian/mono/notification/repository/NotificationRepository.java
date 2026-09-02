package dev.elian.mono.notification.repository;

import dev.elian.mono.notification.domain.Notification;
import dev.elian.mono.notification.domain.NotificationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByStatusAndAttemptsLessThan(NotificationStatus status, int maxAttempts);
}
