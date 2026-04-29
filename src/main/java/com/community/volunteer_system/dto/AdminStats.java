package com.community.volunteer_system.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AdminStats {
    private long totalVolunteers;
    private long totalOrganizers;
    private long totalEvents;
}