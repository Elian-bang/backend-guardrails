package dev.elian.mono.settlement.domain;

import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "daily_settlement",
       uniqueConstraints = @UniqueConstraint(name = "uk_settle", columnNames = {"settle_date", "show_id"}))
public class DailySettlement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "settle_date", nullable = false)
    private LocalDate settleDate;

    @Column(name = "show_id", nullable = false)
    private Long showId;

    private int bookingCount;

    private long totalAmount;

    protected DailySettlement() {}

    public DailySettlement(LocalDate settleDate, Long showId, int bookingCount, long totalAmount) {
        this.settleDate = settleDate;
        this.showId = showId;
        this.bookingCount = bookingCount;
        this.totalAmount = totalAmount;
    }

    public Long getId() { return id; }
    public LocalDate getSettleDate() { return settleDate; }
    public Long getShowId() { return showId; }
    public int getBookingCount() { return bookingCount; }
    public long getTotalAmount() { return totalAmount; }
}
