package com.community.volunteer_system.model;

public enum NotificationType {
    SIGNUP,     // Triggered when a volunteer requests to join
    UPDATE,     // Triggered when event details are modified
    SYSTEM,     // High-priority alerts for the Admin (e.g., new event created)
    REMINDER,   // Automated alerts for upcoming events
    FEEDBACK    // Alerts sent after event completion to request ratings
}