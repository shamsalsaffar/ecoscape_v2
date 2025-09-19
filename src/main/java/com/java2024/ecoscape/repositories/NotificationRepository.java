package com.java2024.ecoscape.repositories;

import com.java2024.ecoscape.models.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
}
