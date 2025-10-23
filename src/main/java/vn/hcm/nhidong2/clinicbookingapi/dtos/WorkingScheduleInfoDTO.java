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
    // THÊM TRƯỜNG NÀY ĐỂ TRUYỀN TÊN ENUM KHÔNG DẤU (MONDAY, TUESDAY...)
    private String dayOfWeekEnglish; 

    // SỬA: BỔ SUNG TRUYỀN dayOfWeekEnglish
    public static WorkingScheduleInfoDTO fromWorkingSchedule(WorkingSchedule schedule, String dayOfWeekVietnamese) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
        String time = schedule.getStartTime().format(formatter) + " - " + schedule.getEndTime().format(formatter);
        return WorkingScheduleInfoDTO.builder()
                .dayOfWeek(dayOfWeekVietnamese)
                .timeSlot(time)
                .dayOfWeekEnglish(schedule.getDayOfWeek().name())
                .build();
    }
}