package dev.elian.fixtures.gaps;

import dev.elian.guard.rules.Fixture;
import dev.elian.mono.booking.domain.Booking;
import dev.elian.mono.booking.repository.BookingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * N+1. 정산을 <b>건별 조회</b>로 짠 버전.
 *
 * <p>정적 분석으로는 못 잡는다 — 반복문 안의 호출을 바이트코드에서 판정하기 어렵고,
 * 무엇보다 <b>반복 횟수를 모른다.</b> 1건이면 문제없고 10만 건이면 장애다.
 * 그래서 R8 은 ArchUnit 이 아니라 <b>런타임 쿼리 수</b>로 잡는다(B-M4).
 */
@Fixture(rule = "R8", expect = Fixture.Expect.KNOWN_GAP,
         note = "반복문 안 단건 조회. 정적 분석 불가 — 쿼리 수 측정으로 잡는다")
@Service
public class R8_반복문_안_단건조회 {

    private final BookingRepository repository;

    public R8_반복문_안_단건조회(BookingRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<Booking> loadEach(List<Long> ids) {
        List<Booking> out = new ArrayList<>();
        for (Long id : ids) {
            repository.findById(id).ifPresent(out::add);   // 건당 쿼리 1회
        }
        return out;
    }
}
