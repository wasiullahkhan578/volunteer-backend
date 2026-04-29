package com.community.volunteer_system.controller;

import com.community.volunteer_system.model.Notification;
import com.community.volunteer_system.model.User;
import com.community.volunteer_system.repository.UserRepository;
import com.community.volunteer_system.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users/notifications")
@CrossOrigin(origins = "http://localhost:5173")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private UserRepository userRepository;

    // --- 1. Fetch All Notifications ---
    @GetMapping("/all")
    public ResponseEntity<List<Notification>> getAllNotifications(Authentication authentication) {
        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<Notification> notes = notificationService.getNotificationsForUser(user.getId());

        // Mark all as read when panel is opened
        notificationService.markAllAsRead(user.getId());

        return ResponseEntity.ok(notes);
    }

    // --- 2. Unread Count for Sidebar Badge ---
    @GetMapping("/unread-count")
    public ResponseEntity<Long> getUnreadCount(Authentication authentication) {
        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        return ResponseEntity.ok(notificationService.getUnreadCount(user.getId()));
    }

    // --- 3. Mark Single Notification as Read ---
    @PutMapping("/{id}/read")
    public ResponseEntity<String> markAsRead(@PathVariable Long id) {
        notificationService.markSingleAsRead(id);
        return ResponseEntity.ok("Notification marked as read");
    }

    // --- 4. NEW: Delete Single Notification ---
    /**
     * This matches your frontend call:
     * fetch(`http://localhost:8080/api/users/notifications/delete/${id}`, { method: 'DELETE' })
     */
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> deleteNotification(@PathVariable Long id) {
        notificationService.deleteNotification(id);
        return ResponseEntity.ok("Notification permanently removed from infrastructure");
    }
}