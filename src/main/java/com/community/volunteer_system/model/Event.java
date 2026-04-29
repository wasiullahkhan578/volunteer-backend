package com.community.volunteer_system.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@Table(name = "events")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Event {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(length = 2000)
    private String description;

    @Column(nullable = false)
    private String location;

    @Column(nullable = false)
    private LocalDateTime eventDate;

    @Column(nullable = false)
    private LocalDateTime endDate;

    private int totalDays;

    private int requiredVolunteers;

    private boolean registrationOpen = true;
    private boolean completed = false;

    @ManyToOne
    @JoinColumn(name = "organizer_id")
    @JsonIgnoreProperties({"password", "participations"}) // Prevent infinite recursion
    private User organizer;

    // --- ADD THIS TO FIX THE DRILL-DOWN ---
    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL)
    @JsonIgnoreProperties("event") // Prevent infinite recursion in JSON
    private List<Participation> participations;
}