package com.community.volunteer_system.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@Table(name = "participation")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Participation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    @JsonIgnoreProperties({"participations", "password"}) // Don't send password or circular refs
    private User user; // The Volunteer

    @ManyToOne
    @JoinColumn(name = "event_id")
    @JsonIgnoreProperties("participations") // Prevent infinite loop back to event list
    private Event event;

    @Enumerated(EnumType.STRING)
    @Builder.Default // Ensures Builder uses the default value
    private ParticipationStatus status = ParticipationStatus.PENDING;

    private String rolePlayed;

    private int daysAttended = 0;

    @Builder.Default
    private LocalDateTime signupDate = LocalDateTime.now();
}