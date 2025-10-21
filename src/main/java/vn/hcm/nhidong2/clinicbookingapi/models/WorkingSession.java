package vn.hcm.nhidong2.clinicbookingapi.models;

import java.time.LocalTime;

public enum WorkingSession {
    SÁNG(LocalTime.of(7, 0), LocalTime.of(11, 0)),
    TRƯA(LocalTime.of(11, 0), LocalTime.of(13, 0)),
    CHIỀU(LocalTime.of(13, 0), LocalTime.of(16, 0)),
    TỐI(LocalTime.of(16, 0), LocalTime.of(19, 0)); 

    private final LocalTime startTime;
    private final LocalTime endTime;

    WorkingSession(LocalTime startTime, LocalTime endTime) {
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }
}