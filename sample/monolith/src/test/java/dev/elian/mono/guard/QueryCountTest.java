package dev.elian.mono.guard;

import dev.elian.guard.rules.QueryCounter;
import dev.elian.mono.booking.domain.Booking;
import dev.elian.mono.booking.domain.Seat;
import dev.elian.mono.booking.domain.Show;
import dev.elian.mono.booking.repository.BookingRepository;
import dev.elian.mono.booking.repository.SeatRepository;
import dev.elian.mono.booking.repository.ShowRepository;
import dev.elian.fixtures.gaps.R8_반복문_안_단건조회;
import dev.elian.mono.settlement.service.SettlementJob;
import jakarta.persistence.EntityManagerFactory;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * B-M4 — 쿼리 수로 N+1 을 잡는다.
 *
 * <p>기준은 절대값이 아니라 <b>건수에 비례하는가</b>다.
 * 100건에 3회면 정상이고 100건에 101회면 N+1 이다.
 */
@SpringBootTest
@Import(R8_반복문_안_단건조회.class)
class QueryCountTest {

    private static final int BOOKINGS = 60;

    @Autowired EntityManagerFactory emf;
    @Autowired ShowRepository showRepository;
    @Autowired SeatRepository seatRepository;
    @Autowired BookingRepository bookingRepository;
    @Autowired SettlementJob settlementJob;
    @Autowired R8_반복문_안_단건조회 nPlusOne;

    private QueryCounter counter;
    private List<Long> bookingIds;

    @BeforeEach
    void setUp() {
        counter = new QueryCounter(emf.unwrap(SessionFactory.class));
        bookingRepository.deleteAll();
        seatRepository.deleteAll();
        showRepository.deleteAll();

        showRepository.save(new Show(1L, "공연 A", 15000));
        showRepository.save(new Show(2L, "공연 B", 20000));
        for (int i = 0; i < BOOKINGS; i++) {
            long showId = (i % 2 == 0) ? 1L : 2L;
            Seat seat = seatRepository.save(new Seat(showId, "S" + i));
            bookingRepository.save(new Booking(showId, seat.getId(), "c" + i, showId == 1L ? 15000 : 20000));
        }
        bookingIds = bookingRepository.findAll().stream().map(Booking::getId).toList();
    }

    @Test
    @DisplayName("정산 배치는 건수에 비례해 쿼리가 늘지 않는다")
    void 정산은_벌크로_돈다() {
        counter.reset();
        int shows = settlementJob.settle(LocalDate.now());

        assertThat(shows).isEqualTo(2);
        // 예약 조회 1회 + 공연별 정산 저장. 예약 건수(60)와 무관해야 한다
        counter.assertNotPerItem(BOOKINGS, 0.2, 10);
    }

    @Test
    @DisplayName("건별 조회는 N+1 로 잡힌다 — 정적 분석이 못 잡는 것을 여기서 잡는다")
    void 건별조회는_잡힌다() {
        counter.reset();
        nPlusOne.loadEach(bookingIds);

        assertThatThrownBy(() -> counter.assertNotPerItem(BOOKINGS, 0.2, 10))
                .isInstanceOf(AssertionError.class)
                .hasMessageContaining("N+1 의심");
    }

    @Test
    @DisplayName("쿼리 수는 실제로 건수에 비례한다")
    void 건별조회는_건수에_비례한다() {
        counter.reset();
        nPlusOne.loadEach(bookingIds.subList(0, 10));
        long ten = counter.count();

        counter.reset();
        nPlusOne.loadEach(bookingIds.subList(0, 40));
        long forty = counter.count();

        assertThat(forty).isGreaterThan(ten * 3);   // 4배 건수면 쿼리도 그만큼
    }
}
