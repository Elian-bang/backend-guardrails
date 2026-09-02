package dev.elian.mono.notification.service;

import dev.elian.mono.notification.domain.Notification;
import dev.elian.mono.notification.domain.NotificationStatus;
import dev.elian.mono.notification.repository.NotificationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 알림의 DB 작업만 담당한다. <b>외부 채널 호출은 여기 없다.</b>
 *
 * <p>발송 흐름은 {@link NotificationSender} 가 조립한다 —
 * 기록(트랜잭션) → 커밋 → 발송(트랜잭션 밖) → 결과 기록(트랜잭션).
 */
@Service
public class NotificationService {

    private final NotificationRepository repository;

    public NotificationService(NotificationRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public Notification create(Long bookingId, String recipient, String message) {
        return repository.save(new Notification(bookingId, recipient, message));
    }

    /**
     * 결과를 기록한다. 건별로 <b>즉시 커밋</b>한다.
     *
     * <p>여러 건을 한 트랜잭션에 모으면, 먼저 끝난 건의 행이 마지막 건이 끝날 때까지 잠긴다.
     * 커밋 비용은 건수에 비례하고 락 손실은 트랜잭션 길이에 비례하므로,
     * <b>락을 잡는 가벼운 쓰기는 건별로 끊는 편이 낫다(R9).</b>
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markResult(Long notificationId, boolean success) {
        repository.findById(notificationId).ifPresent(n -> {
            if (success) n.markSent(); else n.markFailed();
        });
    }

    @Transactional(readOnly = true)
    public List<Notification> findRetryTargets(int maxAttempts) {
        return repository.findByStatusAndAttemptsLessThan(NotificationStatus.FAILED, maxAttempts);
    }

    @Transactional(readOnly = true)
    public List<Notification> findAll() {
        return repository.findAll();
    }
}
