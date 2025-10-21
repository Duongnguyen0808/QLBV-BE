package vn.hcm.nhidong2.clinicbookingapi.dtos;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import vn.hcm.nhidong2.clinicbookingapi.models.WorkingSession; 

import java.time.DayOfWeek;

@Data
public class WorkingScheduleDTO {

    @NotNull(message = "Ngày trong tuần không được để trống")
    private DayOfWeek dayOfWeek;

    // THAY THẾ LocalTime bằng WorkingSession
    @NotNull(message = "Ca làm việc không được để trống")
    private WorkingSession session;

}