package com.java2024.ecoscape.services;

import com.java2024.ecoscape.dto.NotificationDTO;
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

    protected abstract void sendToUser(String username, NotificationDTO notificationDTO);

    protected abstract void broadcast(NotificationDTO notificationDTO);

    private Notification saveNotification(NotificationType notificationType, Booking booking, List<User> users,
                                          NotificationDTO notificationDTO) {
        Notification notification = new Notification();
        notification.setBooking(booking);
        notification.setNotificationType(notificationType);
        notification.setCreatedAt(LocalDate.now());
        notification.setUsers(users);
        notification.setTitle(notificationDTO.getTitle());
        notification.setMessage(notificationDTO.getMessage());
        notification.setSeen(false);
        notificationRepository.save(notification);
        return notification;
    }
    public void notify(NotificationType notificationType, Booking booking) {
        List<User> users = new ArrayList<>();
        users.add(booking.getUser());
        users.add(booking.getListing().getUser());
        if (notificationType.equals(NotificationType.BOOKING_CREATION)) {
            BookingCreationNotificationTemplate bookingCreationNotificationTemplate =
                    new BookingCreationNotificationTemplate();
            NotificationDTO notificationDTO = bookingCreationNotificationTemplate.buildNotification(booking);
            saveNotificationAndNotifyUser(notificationDTO, notificationType, booking, users);

        } else if (notificationType.equals(NotificationType.BOOKING_DETAILS_UPDATE)) {
            BookingDetailsUpdateNotificationTemplate bookingDetailsUpdateNotificationTemplate =
                    new BookingDetailsUpdateNotificationTemplate();
            NotificationDTO notificationDTO = bookingDetailsUpdateNotificationTemplate.buildNotification(booking);
            saveNotificationAndNotifyUser(notificationDTO, notificationType, booking, users);

        } else if (notificationType.equals(NotificationType.BOOKING_CANCELLATION)) {
            BookingCancellationNotificationTemplate bookingCancellationNotificationTemplate =
                    new BookingCancellationNotificationTemplate();
            NotificationDTO notificationDTO = bookingCancellationNotificationTemplate.buildNotification(booking);
            saveNotificationAndNotifyUser(notificationDTO, notificationType, booking, users);
        }
    }

    public void saveNotificationAndNotifyUser(NotificationDTO notificationDTO, NotificationType notificationType, Booking booking, List<User> users) {
        notificationDTO.setNotificationType(notificationType);
        // Save to DB
        Notification notification = saveNotification(notificationType, booking, users, notificationDTO);
        notificationDTO.setId(notification.getId());
        notificationDTO.setCreatedAt(notification.getCreatedAt());
        // Notify the host
        sendToUser(booking.getListing().getUser().getUsername(), notificationDTO);
        // Notify the guest
        sendToUser(booking.getUser().getUsername(), notificationDTO);
    }
}
