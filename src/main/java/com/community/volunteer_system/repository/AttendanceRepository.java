package com.community.volunteer_system.repository;

import com.community.volunteer_system.model.Attendance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, Long> {

    // --- MILESTONE 3: Certification Logic ---
    // Calculates total presence to verify the 75% threshold.
    long countByParticipationIdAndIsPresentTrue(Long participationId);

    // --- MILESTONE 3: Organizer Management ---
    // ADDED: This resolves the "Cannot resolve symbol" error.
    boolean existsByParticipationIdAndAttendanceDate(Long participationId, LocalDate date);

    // Fetches existing records if you need to update a previous mark.
    Optional<Attendance> findByParticipationIdAndAttendanceDate(Long participationId, LocalDate date);

    // Fetches all attendance logs for a specific event to generate participation reports.
    List<Attendance> findByParticipationEventId(Long eventId);
}