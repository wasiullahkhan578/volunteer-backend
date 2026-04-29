package com.community.volunteer_system.repository;

import com.community.volunteer_system.model.Feedback;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface FeedbackRepository extends JpaRepository<Feedback, Long> {

    // Fetches all feedback for a specific event to show in the Admin drill-down
    List<Feedback> findByEventId(Long eventId);

    // Custom query to calculate the average rating for an Organizer
    @Query("SELECT AVG(f.rating) FROM Feedback f WHERE f.event.organizer.id = :organizerId")
    Double getAverageRatingForOrganizer(@Param("organizerId") Long organizerId);
}