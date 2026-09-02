package dev.elian.mono.booking.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "seat",
       uniqueConstraints = @UniqueConstraint(name = "uk_seat", columnNames = {"show_id", "seat_no"}))
public class Seat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "show_id", nullable = false)
    private Long showId;

    @Column(name = "seat_no", nullable = false)
    private String seatNo;

    @Enumerated(EnumType.STRING)
    private SeatStatus status = SeatStatus.OPEN;

    protected Seat() {}

    public Seat(Long showId, String seatNo) {
        this.showId = showId;
        this.seatNo = seatNo;
    }

    /** 좌석을 잡는다. 이미 잡혀 있으면 false. */
    public boolean hold() {
        if (status != SeatStatus.OPEN) return false;
        status = SeatStatus.HELD;
        return true;
    }

    public void release() { status = SeatStatus.OPEN; }

    public Long getId() { return id; }
    public Long getShowId() { return showId; }
    public String getSeatNo() { return seatNo; }
    public SeatStatus getStatus() { return status; }
}
