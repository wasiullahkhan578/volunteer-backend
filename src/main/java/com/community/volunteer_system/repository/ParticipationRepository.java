package com.community.volunteer_system.repository;

import com.community.volunteer_system.model.Participation;
import com.community.volunteer_system.model.ParticipationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface ParticipationRepository extends JpaRepository<Participation, Long> {

    // --- 1. History & Signups ---
    List<Participation> findByUserId(Long userId);

    List<Participation> findByEventId(Long eventId);

    boolean existsByUserIdAndEventId(Long userId, Long eventId);

    Optional<Participation> findByUserIdAndEventId(Long userId, Long eventId);

    // --- 2. Organizer Dashboard Support ---

    /**
     * Finds pending requests for all missions owned by a specific organizer.
     * This powers the "Track Sign-ups" section.
     */
    List<Participation> findByEventOrganizerIdAndStatus(Long organizerId, ParticipationStatus status);

    /**
     * Counts accepted volunteers for a specific event.
     * Used for the "Squad Full" auto-close logic.
     */
    long countByEventIdAndStatus(Long eventId, ParticipationStatus status);

    // --- 3. Attendance Marker Support ---
    /**
     * Filters for only 'ACCEPTED' participations for an event.
     * Prevents pending or cancelled users from appearing in the Attendance Marker.
     */
    List<Participation> findByEventIdAndStatus(Long eventId, ParticipationStatus status);

    // --- 4. Certification Engine Support ---
    @Query("SELECT p FROM Participation p WHERE p.user.id = :userId " +
            "AND p.daysAttended >= (p.event.totalDays * 0.75)")
    List<Participation> findQualifiedParticipations(@Param("userId") Long userId);

    // --- 5. Global Stats Support ---
    long countByUserIdAndDaysAttendedGreaterThan(Long userId, int minDays);
}