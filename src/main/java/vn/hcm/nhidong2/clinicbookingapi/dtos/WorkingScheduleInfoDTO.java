package vn.hcm.nhidong2.clinicbookingapi.dtos;

import lombok.Builder;
import lombok.Data;
import vn.hcm.nhidong2.clinicbookingapi.models.WorkingSchedule;

import java.time.format.DateTimeFormatter;

@Data
@Builder
public class WorkingScheduleInfoDTO {
    private String dayOfWeek;
    private String timeSlot;

    public static WorkingScheduleInfoDTO fromWorkingSchedule(WorkingSchedule schedule, String dayOfWeekVietnamese) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
        String time = schedule.getStartTime().format(formatter) + " - " + schedule.getEndTime().format(formatter);
        return WorkingScheduleInfoDTO.builder()
                .dayOfWeek(dayOfWeekVietnamese)
                .timeSlot(time)
                .build();
    }
}
