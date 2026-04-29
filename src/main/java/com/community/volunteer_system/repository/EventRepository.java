package com.community.volunteer_system.repository;

import com.community.volunteer_system.model.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {

    // --- 1. Organizer Specific Access (Drilldown Support) ---

    // Fetch all missions for "Total Missions" card
    List<Event> findByOrganizerIdOrderByIdDesc(Long organizerId);

    // Fetch only active missions for "Live Quests" card (Registration Open & Not Completed)
    List<Event> findByOrganizerIdAndCompletedFalseAndRegistrationOpenTrue(Long organizerId);

    // --- 2. Real-Time Search (Milestone 2 & 3) ---
    List<Event> findByTitleContainingIgnoreCaseOrLocationContainingIgnoreCase(String title, String location);

    // --- 3. Dashboard Stats Logic (Security Scoped) ---

    // Count for Admin "Superior" Dashboard Stats (Global)
    long countByCompletedFalseAndRegistrationOpenTrue();

    // NEW: Count for individual Organizer Dashboard Stats
    long countByOrganizerId(Long organizerId);

    long countByOrganizerIdAndCompletedFalseAndRegistrationOpenTrue(Long organizerId);

    long countByOrganizerIdAndCompletedTrue(Long organizerId);

    // --- 4. Live Intelligence (Milestone 3) ---

    @Query("SELECT e FROM Event e WHERE e.eventDate <= :now AND (e.endDate IS NULL OR e.endDate >= :now) AND e.completed = false")
    List<Event> findLiveEvents(@Param("now") LocalDateTime now);

    // --- 5. Completion & History ---
    List<Event> findByOrganizerIdAndCompletedTrue(Long organizerId);
}