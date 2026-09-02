package dev.elian.mono.booking.service;

import dev.elian.mono.booking.domain.Booking;
import dev.elian.mono.booking.domain.Seat;
import dev.elian.mono.booking.domain.Show;
import dev.elian.mono.booking.repository.BookingRepository;
import dev.elian.mono.booking.repository.SeatRepository;
import dev.elian.mono.booking.repository.ShowRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 예약 도메인의 트랜잭션 경계.
 *
 * <p>여기서 지키는 것 —
 * <ul>
 *   <li>R3 — 트랜잭션 안에서 외부 호출을 하지 않는다. 알림 발송은 {@link BookingFacade} 가 커밋 후에 한다</li>
 *   <li>R4 — {@code @Transactional} 은 public 에만 붙인다</li>
 *   <li>R2 — 조회는 {@code readOnly = true}</li>
 *   <li>R9 — 좌석 락을 잡는 구간을 짧게 유지한다</li>
 * </ul>
 */
@Service
public class BookingService {

    private final SeatRepository seatRepository;
    private final BookingRepository bookingRepository;
    private final ShowRepository showRepository;

    public BookingService(SeatRepository seatRepository,
                          BookingRepository bookingRepository,
                          ShowRepository showRepository) {
        this.seatRepository = seatRepository;
        this.bookingRepository = bookingRepository;
        this.showRepository = showRepository;
    }

    /**
     * 좌석을 잡고 예약을 만든다.
     *
     * <p>이 트랜잭션은 <b>DB 작업만</b> 한다. 알림은 커밋 후에 나간다.
     */
    @Transactional
    public Booking reserve(Long showId, String seatNo, String customerId) {
        Show show = showRepository.findById(showId)
                .orElseThrow(() -> new IllegalArgumentException("공연 없음: " + showId));

        Seat seat = seatRepository.findForUpdate(showId, seatNo)
                .orElseThrow(() -> new IllegalArgumentException("좌석 없음: " + seatNo));

        if (!seat.hold()) {
            throw new SeatAlreadyTakenException(showId, seatNo);
        }

        return bookingRepository.save(new Booking(showId, seat.getId(), customerId, show.getSeatPrice()));
    }

    @Transactional(readOnly = true)
    public List<Booking> findByCustomer(String customerId) {
        return bookingRepository.findByCustomerId(customerId);
    }

    @Transactional(readOnly = true)
    public List<Seat> findSeats(Long showId) {
        return seatRepository.findByShowId(showId);
    }

    @Transactional
    public void cancel(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("예약 없음: " + bookingId));
        booking.cancel();
        seatRepository.findById(booking.getSeatId()).ifPresent(Seat::release);
    }
}
