package com.java2024.ecoscape.repositories;

import com.java2024.ecoscape.models.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findAllByUsers_Id(Long userId);

}
