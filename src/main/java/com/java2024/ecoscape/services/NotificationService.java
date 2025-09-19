package com.java2024.ecoscape.services;

import com.java2024.ecoscape.models.Booking;
import com.java2024.ecoscape.models.Notification;
import com.java2024.ecoscape.models.NotificationType;
import com.java2024.ecoscape.models.User;
import com.java2024.ecoscape.repositories.NotificationRepository;
import com.java2024.ecoscape.websockets.templates.BookingCancellationNotificationTemplate;
import com.java2024.ecoscape.websockets.templates.BookingCreationNotificationTemplate;
import com.java2024.ecoscape.websockets.templates.BookingDetailsUpdateNotificationTemplate;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public abstract class NotificationService {
    private final NotificationRepository notificationRepository;

    protected NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    protected abstract void sendToUser(String username, String message);
    protected abstract void broadcast(String message);

    private void saveNotification(NotificationType notificationType, Booking booking, List<User> users,
                                  String header, String body, String footer) {
        Notification notification = new Notification();
        notification.setBooking(booking);
        notification.setNotificationType(notificationType);
        notification.setCreatedAt(LocalDate.now());
        notification.setUsers(users);
        notification.setMessage(body + "\n" + footer);
        notification.setTitle(header);
        notification.setSeen(false);
        notificationRepository.save(notification);
    }

    public void notify(NotificationType notificationType, Booking booking) {
        List<User> users = new ArrayList<>();
        users.add(booking.getUser());
        users.add(booking.getListing().getUser());
        if(notificationType.equals(NotificationType.BOOKING_CREATION)) {
            BookingCreationNotificationTemplate bookingCreationNotificationTemplate =
                    new BookingCreationNotificationTemplate();
            // Notify the host
            sendToUser(booking.getListing().getUser().getUsername(),
                    bookingCreationNotificationTemplate.buildMessage(booking));
            // Notify the guest
            sendToUser(booking.getUser().getUsername(),
                    bookingCreationNotificationTemplate.buildMessage(booking));
            saveNotification(notificationType, booking, users, bookingCreationNotificationTemplate.getHeader(),
                    bookingCreationNotificationTemplate.getBody(), bookingCreationNotificationTemplate.getFooter());

        } else if(notificationType.equals(NotificationType.BOOKING_DETAILS_UPDATE)) {
            BookingDetailsUpdateNotificationTemplate bookingDetailsUpdateNotificationTemplate =
                    new BookingDetailsUpdateNotificationTemplate();
            sendToUser(booking.getListing().getUser().getUsername(),
                    bookingDetailsUpdateNotificationTemplate.buildMessage(booking));
            // Notify the host
            sendToUser(booking.getListing().getUser().getUsername(),
                    bookingDetailsUpdateNotificationTemplate.buildMessage(booking));
            // Notify the guest
            sendToUser(booking.getUser().getUsername(),
                    bookingDetailsUpdateNotificationTemplate.buildMessage(booking));
            saveNotification(notificationType, booking, users, bookingDetailsUpdateNotificationTemplate.getHeader(),
                    bookingDetailsUpdateNotificationTemplate.getBody(), bookingDetailsUpdateNotificationTemplate.getFooter());

        } else if(notificationType.equals(NotificationType.BOOKING_CANCELLATION)) {
            BookingCancellationNotificationTemplate bookingCancellationNotificationTemplate =
                    new BookingCancellationNotificationTemplate();
            sendToUser(booking.getListing().getUser().getUsername(),
                    bookingCancellationNotificationTemplate.buildMessage(booking));
            // Notify the host
            sendToUser(booking.getListing().getUser().getUsername(),
                    bookingCancellationNotificationTemplate.buildMessage(booking));
            // Notify the guest
            sendToUser(booking.getUser().getUsername(),
                    bookingCancellationNotificationTemplate.buildMessage(booking));
            saveNotification(notificationType, booking, users, bookingCancellationNotificationTemplate.getHeader(),
                    bookingCancellationNotificationTemplate.getBody(), bookingCancellationNotificationTemplate.getFooter());


        }
    }
}
