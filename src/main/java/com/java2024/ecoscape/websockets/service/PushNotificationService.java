package com.java2024.ecoscape.websockets.service;

import com.java2024.ecoscape.dto.NotificationDTO;
import com.java2024.ecoscape.models.Notification;
import com.java2024.ecoscape.models.User;
import com.java2024.ecoscape.repositories.NotificationRepository;
import com.java2024.ecoscape.services.AuthenticationService;
import com.java2024.ecoscape.services.NotificationService;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

@Service
public class PushNotificationService extends NotificationService {

    private final SimpMessagingTemplate messagingTemplate;
    private final AuthenticationService authenticationService;
    private final NotificationRepository notificationRepository;

    public PushNotificationService(SimpMessagingTemplate messagingTemplate,
                                   /* We must send notificationRepository to be used by the parent (abstract class)*/
                                   NotificationRepository notificationRepository, AuthenticationService authenticationService, NotificationRepository notificationRepository1) {
        super(notificationRepository);
        this.messagingTemplate = messagingTemplate;
        this.authenticationService = authenticationService;
        this.notificationRepository = notificationRepository1;
    }


    @Override
    protected void sendToUser(String username, NotificationDTO notificationDTO) {
        messagingTemplate.convertAndSendToUser(username, "/queue/notifications", notificationDTO);
    }

    @Override
    protected void broadcast(NotificationDTO notificationDTO) {
        messagingTemplate.convertAndSend("/topic/notifications", notificationDTO);
    }


    public List<NotificationDTO> getAllNotificationsByUserId (Long userId){
        User authenticateUser = authenticationService.authenticateMethods();
        List<Notification> notifications = notificationRepository.findAllByUsers_Id(userId);
        return mapToDTO(notifications);
    }

    private List<NotificationDTO> mapToDTO(List<Notification> notificationList) {
        List<NotificationDTO> notifications = new ArrayList<>();
        for(Notification notification : notificationList) {
            notifications.add(mapToDTO(notification));
        }
        return notifications;
    }

    private NotificationDTO mapToDTO(Notification notificationEntity) {
        NotificationDTO notificationDTO = new NotificationDTO();
        notificationDTO.setCreatedAt(notificationEntity.getCreatedAt());
        notificationDTO.setId(notificationEntity.getId());
        notificationDTO.setBookingId(notificationEntity.getBooking().getId());
        notificationDTO.setNotificationType(notificationEntity.getNotificationType());
        notificationDTO.setMessage(notificationEntity.getMessage());
        notificationDTO.setTitle(notificationEntity.getTitle());
        notificationDTO.setSeen(notificationEntity.getSeen());
        return notificationDTO;
    }

    public NotificationDTO markPushNotificationAsSeen(Long notificationId){
        Notification notification = notificationRepository.findById(notificationId).orElseThrow(() -> new NoSuchElementException("Notification not found"));
        notification.setSeen(true);
        notificationRepository.save(notification);
        NotificationDTO notificationDTO = mapToDTO(notification);
        return notificationDTO;
    }

}


