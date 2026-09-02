package dev.elian.mono.settlement.service;

import dev.elian.mono.settlement.domain.DailySettlement;
import dev.elian.mono.settlement.repository.DailySettlementRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/** 정산 결과의 쓰기 경계. 집계 계산은 여기 없다 — 계산은 트랜잭션 밖에서 끝낸다. */
@Service
public class SettlementService {

    private final DailySettlementRepository repository;

    public SettlementService(DailySettlementRepository repository) {
        this.repository = repository;
    }

    /**
     * 정산 한 건을 <b>즉시 커밋</b>한다.
     *
     * <p>여러 공연분을 한 트랜잭션에 모으면, 먼저 계산된 공연의 행이
     * 마지막 공연까지 끝날 때까지 잠긴다. 집계가 무거울수록 그 시간이 길어진다.
     * <b>락을 잡는 가벼운 쓰기와 무거운 계산을 같은 트랜잭션에 두지 않는다(R9).</b>
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void save(DailySettlement settlement) {
        repository.save(settlement);
    }

    @Transactional(readOnly = true)
    public List<DailySettlement> findByDate(LocalDate date) {
        return repository.findBySettleDate(date);
    }
}
