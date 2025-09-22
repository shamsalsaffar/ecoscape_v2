package com.java2024.ecoscape.controllers;

import com.java2024.ecoscape.dto.NotificationDTO;
import com.java2024.ecoscape.websockets.service.PushNotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
public class NotificationsController {
    private final PushNotificationService notificationService;
    private final PushNotificationService pushNotificationService;

    public NotificationsController(PushNotificationService notificationService, PushNotificationService pushNotificationService) {
        this.notificationService = notificationService;
        this.pushNotificationService = pushNotificationService;
    }

    @GetMapping("/{userId}")
    public ResponseEntity<List<NotificationDTO>> requestHost(@PathVariable(name = "userId") Long userId) {
        return ResponseEntity.ok(notificationService.getAllNotificationsByUserId(userId));
    }
}
