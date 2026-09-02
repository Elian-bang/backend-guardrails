package dev.elian.mono.booking.repository;

import dev.elian.mono.booking.domain.Booking;
import dev.elian.mono.booking.domain.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findByCustomerId(String customerId);

    /** 정산이 쓰는 조회. 기간으로 한 번에 가져온다 — 건별 조회는 R8 위반이다. */
    List<Booking> findByStatusAndCreatedAtBetween(BookingStatus status, LocalDateTime from, LocalDateTime to);
}
