package com.java2024.ecoscape.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.Length;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "notifications")
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToMany(fetch = FetchType.EAGER)
    @NotNull(message = "Notification must have at least one recipient")
    @JoinTable(
            name = "user_notifications",
            joinColumns = @JoinColumn(name = "notification_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private List<User> users;

    @NotNull(message = "Notification must have title")
    @NotEmpty(message = "Notification title can not be empty")
    @Length(max = 100, message = "Notification title cannot exceed 100 characters")
    private String title;

    @NotNull(message = "Notification must have message")
    @NotEmpty(message = "Notification message can not be empty")
    @Length(max = 2048, message = "Notification title cannot exceed 2048 characters")
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(name = "notification_type", nullable = false)
    private NotificationType notificationType;

    @NotNull(message = "Notification must have creation date")
    @CreatedDate //fylls i automatiskt av Spring när objektet skapas, behöver testas
    @Column(nullable = false, updatable = false, name = ("created_at"))
    private LocalDate createdAt;


    @Column(nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE") //blir automatisk false när objektet skaffas, behöver testas
    private Boolean seen;

    @ManyToOne
    @JoinColumn(name = "booking_id", nullable = true) //kan vara null, t.ex. icke booking relaterade notifikation, informativa notifikationer
    private Booking booking;

    public Notification() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public @NotNull(message = "Notification must have at least one recipient") List<User> getUsers() {
        return users;
    }

    public void setUsers(@NotNull(message = "Notification must have at least one recipient") List<User> users) {
        this.users = users;
    }

    public @NotNull(message = "Notification must have title") @NotEmpty(message = "Notification title can not be empty") @Length(max = 100, message = "Notification title cannot exceed 100 characters") String getTitle() {
        return title;
    }

    public void setTitle(@NotNull(message = "Notification must have title") @NotEmpty(message = "Notification title can not be empty") @Length(max = 100, message = "Notification title cannot exceed 100 characters") String title) {
        this.title = title;
    }

    public @NotNull(message = "Notification must have message") @NotEmpty(message = "Notification message can not be empty") @Length(max = 2048, message = "Notification title cannot exceed 2048 characters") String getMessage() {
        return message;
    }

    public void setMessage(@NotNull(message = "Notification must have message") @NotEmpty(message = "Notification message can not be empty") @Length(max = 2048, message = "Notification title cannot exceed 2048 characters") String message) {
        this.message = message;
    }

    public NotificationType getNotificationType() {
        return notificationType;
    }

    public void setNotificationType(NotificationType notificationType) {
        this.notificationType = notificationType;
    }

    public @NotNull(message = "Notification must have creation date") LocalDate getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(@NotNull(message = "Notification must have creation date") LocalDate createdAt) {
        this.createdAt = createdAt;
    }

    public Boolean getSeen() {
        return seen;
    }

    public void setSeen(Boolean seen) {
        this.seen = seen;
    }

    public Booking getBooking() {
        return booking;
    }

    public void setBooking(Booking booking) {
        this.booking = booking;
    }
}
