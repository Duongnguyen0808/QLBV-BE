package vn.hcm.nhidong2.clinicbookingapi.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import vn.hcm.nhidong2.clinicbookingapi.dtos.DoctorSearchResultDTO;
import vn.hcm.nhidong2.clinicbookingapi.dtos.WorkingScheduleInfoDTO;
import vn.hcm.nhidong2.clinicbookingapi.models.Doctor;
import vn.hcm.nhidong2.clinicbookingapi.services.DoctorService;

import java.time.DayOfWeek;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/public")
@RequiredArgsConstructor
public class PublicApiController {

    private final DoctorService doctorService;

    private static final Map<DayOfWeek, String> DAY_OF_WEEK_TRANSLATIONS = new LinkedHashMap<>();
    static {
        DAY_OF_WEEK_TRANSLATIONS.put(DayOfWeek.MONDAY, "Thứ Hai");
        DAY_OF_WEEK_TRANSLATIONS.put(DayOfWeek.TUESDAY, "Thứ Ba");
        DAY_OF_WEEK_TRANSLATIONS.put(DayOfWeek.WEDNESDAY, "Thứ Tư");
        DAY_OF_WEEK_TRANSLATIONS.put(DayOfWeek.THURSDAY, "Thứ Năm");
        DAY_OF_WEEK_TRANSLATIONS.put(DayOfWeek.FRIDAY, "Thứ Sáu");
        DAY_OF_WEEK_TRANSLATIONS.put(DayOfWeek.SATURDAY, "Thứ Bảy");
        DAY_OF_WEEK_TRANSLATIONS.put(DayOfWeek.SUNDAY, "Chủ Nhật");
    }

    @GetMapping("/doctors/search")
    public ResponseEntity<List<DoctorSearchResultDTO>> searchDoctors(
            @RequestParam Optional<String> name,
            @RequestParam Optional<Long> specialtyId,
            @RequestParam Optional<DayOfWeek> dayOfWeek
    ) {
        List<Doctor> foundDoctors = doctorService.searchDoctors(name, specialtyId, dayOfWeek);

        List<DoctorSearchResultDTO> resultsDTO = foundDoctors.stream().map(doctor -> {
            List<WorkingScheduleInfoDTO> scheduleInfos = doctor.getWorkingSchedules().stream()
                    .sorted(Comparator.comparing(s -> s.getDayOfWeek().getValue()))
                    .map(ws -> WorkingScheduleInfoDTO.fromWorkingSchedule(ws, DAY_OF_WEEK_TRANSLATIONS.get(ws.getDayOfWeek())))
                    .collect(Collectors.toList());
            return DoctorSearchResultDTO.fromDoctor(doctor, scheduleInfos);
        }).collect(Collectors.toList());

        return ResponseEntity.ok(resultsDTO);
    }
}
