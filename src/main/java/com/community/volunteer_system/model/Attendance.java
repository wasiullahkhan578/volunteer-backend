package com.community.volunteer_system.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "attendance")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Attendance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "participation_id", nullable = false)
    private Participation participation; // Links to the volunteer-event pair

    @Column(nullable = false)
    private LocalDate attendanceDate; // The specific day being marked

    private boolean isPresent; // True = Present, False = Absent

    private String remarks; // Optional notes (e.g., "Late arrival")
}