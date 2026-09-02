package dev.elian.mono.booking.service;

import dev.elian.mono.booking.domain.Booking;
import dev.elian.mono.notification.service.NotificationSender;
import org.springframework.stereotype.Service;

/**
 * 유스케이스 조립. <b>{@code @Transactional} 이 없다.</b>
 *
 * <p>예약 저장은 {@link BookingService} 가 트랜잭션 안에서 하고,
 * 알림은 그 트랜잭션이 <b>커밋된 뒤에</b> 나간다.
 *
 * <p>둘을 한 트랜잭션에 넣으면 두 가지가 동시에 깨진다 —
 * 좌석 락을 쥔 채 채널 응답을 기다리게 되고(R3),
 * 예약이 롤백돼도 알림은 이미 나간 상태가 된다(R10).
 */
@Service
public class BookingFacade {

    private final BookingService bookingService;
    private final NotificationSender notificationSender;

    public BookingFacade(BookingService bookingService, NotificationSender notificationSender) {
        this.bookingService = bookingService;
        this.notificationSender = notificationSender;
    }

    // 데모용 고의 위반 — 예약과 알림을 한 트랜잭션에 넣었다.
    // 좌석 락을 쥔 채 채널 응답을 기다리고(R3), 예약이 롤백돼도 알림은 이미 나간다(R10).
    @org.springframework.transaction.annotation.Transactional
    public Booking book(Long showId, String seatNo, String customerId) {
        Booking booking = bookingService.reserve(showId, seatNo, customerId);
        new org.springframework.web.client.RestTemplate()
                .getForObject("https://channel.example/send", String.class);
        notificationSender.sendBookingConfirmed(
                booking.getId(), customerId, "예약이 확정되었습니다. 좌석 " + seatNo);
        return booking;
    }
}
