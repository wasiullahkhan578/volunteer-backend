package com.community.volunteer_system.dto;

import lombok.Data;
import java.util.List;

@Data
public class AttendanceRequest {
    private Long eventId;
    private List<VolunteerStatus> records;

    @Data
    public static class VolunteerStatus {
        private Long participationId;
        private boolean present;
    }
}