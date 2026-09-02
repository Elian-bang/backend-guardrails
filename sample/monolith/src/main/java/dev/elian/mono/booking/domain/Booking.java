package dev.elian.mono.booking.domain;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "booking")
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long showId;

    @Column(nullable = false)
    private Long seatId;

    @Column(nullable = false)
    private String customerId;

    @Column(nullable = false)
    private int amount;

    @Enumerated(EnumType.STRING)
    private BookingStatus status = BookingStatus.CONFIRMED;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    protected Booking() {}

    public Booking(Long showId, Long seatId, String customerId, int amount) {
        this.showId = showId;
        this.seatId = seatId;
        this.customerId = customerId;
        this.amount = amount;
    }

    public void cancel() { status = BookingStatus.CANCELED; }

    public Long getId() { return id; }
    public Long getShowId() { return showId; }
    public Long getSeatId() { return seatId; }
    public String getCustomerId() { return customerId; }
    public int getAmount() { return amount; }
    public BookingStatus getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
