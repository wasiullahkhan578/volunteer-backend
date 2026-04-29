package com.community.volunteer_system.model;

public enum ParticipationStatus {
    PENDING,    // Initial state when volunteer signs up
    ACCEPTED,   // Organizer selects this volunteer
    DENIED,     // Organizer rejects the request
    CANCELLED,  // Volunteer withdraws
    COMPLETED   // Event is finished and attendance was recorded
}