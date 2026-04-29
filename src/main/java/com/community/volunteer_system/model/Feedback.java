package com.community.volunteer_system.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "feedback")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Feedback {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // The event being rated (Milestone 3)
    @ManyToOne
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    // The volunteer providing the feedback (Milestone 3)
    @ManyToOne
    @JoinColumn(name = "volunteer_id", nullable = false)
    private User volunteer;

    // Rating from 1 to 5 stars
    @Column(nullable = false)
    private int rating;

    // Detailed comments from the volunteer
    @Column(length = 1000)
    private String comment;

    private LocalDateTime createdAt = LocalDateTime.now();
}