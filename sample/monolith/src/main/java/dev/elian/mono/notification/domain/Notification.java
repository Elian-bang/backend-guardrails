package dev.elian.mono.notification.domain;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "notification")
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long bookingId;

    @Column(nullable = false)
    private String recipient;

    @Column(nullable = false, length = 500)
    private String message;

    @Enumerated(EnumType.STRING)
    private NotificationStatus status = NotificationStatus.PENDING;

    private int attempts = 0;

    private LocalDateTime sentAt;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    protected Notification() {}

    public Notification(Long bookingId, String recipient, String message) {
        this.bookingId = bookingId;
        this.recipient = recipient;
        this.message = message;
    }

    public void markSent() {
        status = NotificationStatus.SENT;
        sentAt = LocalDateTime.now();
        attempts++;
    }

    public void markFailed() {
        status = NotificationStatus.FAILED;
        attempts++;
    }

    public Long getId() { return id; }
    public Long getBookingId() { return bookingId; }
    public String getRecipient() { return recipient; }
    public String getMessage() { return message; }
    public NotificationStatus getStatus() { return status; }
    public int getAttempts() { return attempts; }
}
