package com.community.volunteer_system.repository;

import com.community.volunteer_system.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    // --- MILESTONE 3: Dashboard Inbox Logic ---
    // Fetches unread alerts for a specific user, sorted by most recent
    List<Notification> findByRecipientIdAndIsReadFalseOrderByCreatedAtDesc(Long userId);

    // --- MILESTONE 3: Superior Admin View ---
    // Fetches all high-priority alerts across the system for the Admin
    List<Notification> findByPriorityTrueOrderByCreatedAtDesc();

    // Fetches all notifications for a specific user (History)
    List<Notification> findByRecipientIdOrderByCreatedAtDesc(Long userId);
}