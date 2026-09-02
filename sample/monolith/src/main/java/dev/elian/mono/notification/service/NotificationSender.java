package dev.elian.mono.notification.service;

import dev.elian.mono.notification.channel.ChannelClient;
import dev.elian.mono.notification.domain.Notification;
import org.springframework.stereotype.Service;

/**
 * 발송 흐름을 조립한다. <b>이 클래스에는 {@code @Transactional} 이 없다.</b>
 *
 * <pre>
 *   기록(트랜잭션) → 커밋 → 발송(트랜잭션 밖) → 결과 기록(트랜잭션)
 * </pre>
 *
 * <p>순서가 이래야 하는 이유 —
 * <ul>
 *   <li><b>R3</b> 채널 호출이 트랜잭션 밖이라 락과 커넥션을 붙잡지 않는다</li>
 *   <li><b>R10</b> 커밋 후에 나가므로, 롤백됐는데 알림만 나가는 일이 없다</li>
 *   <li><b>R7</b> 재시도 대기가 트랜잭션 밖이라 락을 쥔 채 잠들지 않는다</li>
 * </ul>
 */
@Service
public class NotificationSender {

    private final NotificationService notificationService;
    private final ChannelClient channelClient;

    public NotificationSender(NotificationService notificationService, ChannelClient channelClient) {
        this.notificationService = notificationService;
        this.channelClient = channelClient;
    }

    /** 예약 확정 알림. 호출자는 이미 커밋을 마친 상태여야 한다. */
    public void sendBookingConfirmed(Long bookingId, String recipient, String message) {
        Notification saved = notificationService.create(bookingId, recipient, message);   // 트랜잭션 1 — 커밋됨
        boolean ok = channelClient.send(recipient, message);                              // 트랜잭션 밖
        notificationService.markResult(saved.getId(), ok);                                // 트랜잭션 2 — 건별 커밋
    }

    /**
     * 실패분 재전송.
     *
     * <p>대기는 트랜잭션 밖에서 한다. 트랜잭션 안에서 백오프를 걸면 <b>락을 쥔 채로 잠든다(R7)</b>.
     */
    public int retryFailed(int maxAttempts, long backoffMillis) {
        int sent = 0;
        for (Notification n : notificationService.findRetryTargets(maxAttempts)) {
            sleepQuietly(backoffMillis);                                   // 트랜잭션 밖
            boolean ok = channelClient.send(n.getRecipient(), n.getMessage());
            notificationService.markResult(n.getId(), ok);                 // 건별 커밋
            if (ok) sent++;
        }
        return sent;
    }

    private static void sleepQuietly(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
