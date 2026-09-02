package dev.elian.mono.booking.repository;

import dev.elian.mono.booking.domain.Seat;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SeatRepository extends JpaRepository<Seat, Long> {

    List<Seat> findByShowId(Long showId);

    /**
     * 좌석을 비관적 락으로 잡는다. 같은 좌석에 동시 요청이 오면 한쪽만 통과한다.
     *
     * <p>락을 잡는 구간이므로 <b>이 트랜잭션은 짧아야 한다.</b>
     * 외부 호출이나 대기가 여기 끼면 그만큼 다른 요청이 줄을 선다.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from Seat s where s.showId = :showId and s.seatNo = :seatNo")
    Optional<Seat> findForUpdate(@Param("showId") Long showId, @Param("seatNo") String seatNo);
}
