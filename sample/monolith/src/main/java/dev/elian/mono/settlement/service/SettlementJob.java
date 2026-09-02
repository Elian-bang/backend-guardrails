package dev.elian.mono.settlement.service;

import dev.elian.mono.booking.domain.Booking;
import dev.elian.mono.booking.domain.BookingStatus;
import dev.elian.mono.booking.service.BookingQueryService;
import dev.elian.mono.settlement.domain.DailySettlement;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 일일 정산. <b>{@code @Transactional} 이 없다.</b>
 *
 * <p>지키는 것 —
 * <ul>
 *   <li><b>R8</b> 예약을 <b>기간으로 한 번에</b> 가져온다. 건별 조회는 N+1 이다</li>
 *   <li><b>R9</b> 집계는 메모리에서 끝내고, 락을 잡는 저장만 건별로 커밋한다</li>
 * </ul>
 */
@Service
public class SettlementJob {

    private final BookingQueryService bookingQueryService;
    private final SettlementService settlementService;

    public SettlementJob(BookingQueryService bookingQueryService, SettlementService settlementService) {
        this.bookingQueryService = bookingQueryService;
        this.settlementService = settlementService;
    }

    public int settle(LocalDate date) {
        List<Booking> bookings = bookingQueryService.findConfirmedOn(date, BookingStatus.CONFIRMED);

        Map<Long, long[]> byShow = new LinkedHashMap<>();          // showId -> [건수, 금액]
        for (Booking b : bookings) {
            long[] acc = byShow.computeIfAbsent(b.getShowId(), k -> new long[2]);
            acc[0]++;
            acc[1] += b.getAmount();
        }

        byShow.forEach((showId, acc) ->
                settlementService.save(new DailySettlement(date, showId, (int) acc[0], acc[1])));

        return byShow.size();
    }
}
