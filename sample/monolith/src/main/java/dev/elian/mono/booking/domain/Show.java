package dev.elian.mono.booking.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "show_event")
public class Show {

    @Id
    private Long id;

    private String title;

    private int seatPrice;

    protected Show() {}

    public Show(Long id, String title, int seatPrice) {
        this.id = id;
        this.title = title;
        this.seatPrice = seatPrice;
    }

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public int getSeatPrice() { return seatPrice; }
}
