package dev.elian.mono.settlement.repository;

import dev.elian.mono.settlement.domain.DailySettlement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface DailySettlementRepository extends JpaRepository<DailySettlement, Long> {

    List<DailySettlement> findBySettleDate(LocalDate settleDate);
}
