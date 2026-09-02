package dev.elian.mono.booking.service;

import dev.elian.mono.booking.domain.Booking;
import dev.elian.mono.booking.domain.BookingStatus;
import dev.elian.mono.booking.repository.BookingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * 다른 도메인(정산)이 예약을 읽는 통로.
 *
 * <p>정산이 {@code BookingRepository} 를 직접 부르면 계층이 무너진다(R6).
 * 도메인 간 접근은 서비스를 통한다 — MSA 로 쪼갤 때 이 자리가 HTTP 경계가 된다.
 */
@Service
public class BookingQueryService {

    private final BookingRepository bookingRepository;

    public BookingQueryService(BookingRepository bookingRepository) {
        this.bookingRepository = bookingRepository;
    }

    /** 기간으로 한 번에 가져온다. 건별 조회는 N+1 이다(R8). */
    @Transactional(readOnly = true)
    public List<Booking> findConfirmedOn(LocalDate date, BookingStatus status) {
        return bookingRepository.findByStatusAndCreatedAtBetween(
                status, date.atStartOfDay(), date.atTime(LocalTime.MAX));
    }
}
