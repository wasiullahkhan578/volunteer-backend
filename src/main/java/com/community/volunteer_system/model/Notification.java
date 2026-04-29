package com.community.volunteer_system.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User recipient; // Can be Admin, Organizer, or Volunteer

    private String title;
    private String message;

    @Enumerated(EnumType.STRING)
    private NotificationType type; // ALERT, UPDATE, SIGNUP, SYSTEM

    private boolean isRead = false;
    private boolean priority = false; // True for Admin notifications

    private LocalDateTime createdAt = LocalDateTime.now();

    // In your Backend Notification Entity

}