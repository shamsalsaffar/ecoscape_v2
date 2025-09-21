package com.java2024.ecoscape.controllers;

import com.java2024.ecoscape.dto.NotificationDTO;
import com.java2024.ecoscape.websockets.service.PushNotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
public class NotificationsController {
    private final PushNotificationService notificationService;

    public NotificationsController(PushNotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping("/{userId}")
    public ResponseEntity<List<NotificationDTO>> requestHost(@PathVariable(name = "userId") Long userId) {
        return ResponseEntity.ok(notificationService.getAllNotificationsByUserId(userId));
    }
}
