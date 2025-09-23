package com.java2024.ecoscape.websockets.controllers;

import com.java2024.ecoscape.dto.NotificationDTO;
import com.java2024.ecoscape.websockets.service.PushNotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pushnotifications")

public class PushNotificationController {

    private final PushNotificationService pushNotificationService;

    public PushNotificationController(PushNotificationService pushNotificationService) {
        this.pushNotificationService = pushNotificationService;
    }

    @PatchMapping("/{notificationId}/seen")
    public ResponseEntity<NotificationDTO> markAsSeen(@PathVariable Long notificationId) {
        NotificationDTO updatedNotification = pushNotificationService.markPushNotificationAsSeen(notificationId);
        return ResponseEntity.ok(updatedNotification);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<List<NotificationDTO>> getAllNotificationsByUserId(@PathVariable(name = "userId") Long userId) {
        return ResponseEntity.ok(pushNotificationService.getPushAllNotificationsByUserId(userId));
    }
}
