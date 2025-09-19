package com.java2024.ecoscape.websockets.service;

import com.java2024.ecoscape.repositories.NotificationRepository;
import com.java2024.ecoscape.services.NotificationService;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class PushNotificationService extends NotificationService {

    private final SimpMessagingTemplate messagingTemplate;

    public PushNotificationService(SimpMessagingTemplate messagingTemplate,
                                   /* We must send notificationRepository to be used by the parent (abstract class)*/
                                   NotificationRepository notificationRepository) {
        super(notificationRepository);
        this.messagingTemplate = messagingTemplate;
    }


    @Override
    protected void sendToUser(String username, String message) {
        messagingTemplate.convertAndSendToUser(username, "/queue/notifications", message);
    }

    @Override
    protected void broadcast(String message) {
        messagingTemplate.convertAndSend("/topic/notifications", message);
    }
}


