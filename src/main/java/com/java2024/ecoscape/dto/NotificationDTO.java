package com.java2024.ecoscape.dto;

import com.java2024.ecoscape.models.NotificationType;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public class NotificationDTO {

    private Long id;

    @NotNull(message = "Notification must have title")
    private String title;

    @NotNull(message = "Notification must have message")
    private String message;

    @NotNull(message = "Notification must have type")
    private NotificationType notificationType;

    @NotNull(message = "Notification must have creation date")
    private LocalDate createdAt;

    private Boolean seen = false;

    private Long bookingId;

    public NotificationDTO(Long id, String title, String message, NotificationType notificationType, LocalDate createdAt, Boolean seen, Long bookingId) {
        this.id = id;
        this.title = title;
        this.message = message;
        this.notificationType = notificationType;
        this.createdAt = createdAt;
        this.seen = seen;
        this.bookingId = bookingId;
    }

    public NotificationDTO() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public NotificationType getNotificationType() {
        return notificationType;
    }

    public void setNotificationType(NotificationType notificationType) {
        this.notificationType = notificationType;
    }

    public LocalDate getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDate createdAt) {
        this.createdAt = createdAt;
    }

    public Boolean getSeen() {
        return seen;
    }

    public void setSeen(Boolean seen) {
        this.seen = seen;
    }

    public Long getBookingId() {
        return bookingId;
    }

    public void setBookingId(Long bookingId) {
        this.bookingId = bookingId;
    }
}
