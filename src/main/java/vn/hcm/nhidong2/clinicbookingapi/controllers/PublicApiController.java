package vn.hcm.nhidong2.clinicbookingapi.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.RequiredArgsConstructor;
import vn.hcm.nhidong2.clinicbookingapi.dtos.DoctorSearchResultDTO;
import vn.hcm.nhidong2.clinicbookingapi.dtos.WorkingScheduleInfoDTO;
import vn.hcm.nhidong2.clinicbookingapi.dtos.DoctorRatingDTO;
import vn.hcm.nhidong2.clinicbookingapi.models.Doctor;
import vn.hcm.nhidong2.clinicbookingapi.models.DoctorReview;
import vn.hcm.nhidong2.clinicbookingapi.repositories.DoctorReviewRepository;
import vn.hcm.nhidong2.clinicbookingapi.services.DoctorService;

import java.time.DayOfWeek;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/public")
@RequiredArgsConstructor
@Tag(name = "Public API", description = "Các API công khai")
public class PublicApiController {

    private final DoctorService doctorService;
    private final DoctorReviewRepository doctorReviewRepository;

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

            // Tính rating trung bình và số lượng đánh giá
            List<DoctorReview> reviews = doctorReviewRepository.findByDoctor_Id(doctor.getId());
            int reviewCount = reviews.size();
            Double rating = reviews.isEmpty() ? null :
                    Math.round(reviews.stream().mapToInt(DoctorReview::getRating).average().orElse(0.0) * 10.0) / 10.0;

            return DoctorSearchResultDTO.fromDoctor(doctor, scheduleInfos, rating, reviewCount);
        }).collect(Collectors.toList());

        return ResponseEntity.ok(resultsDTO);
    }

    @GetMapping("/doctors/{doctorId}/rating")
    public ResponseEntity<DoctorRatingDTO> getDoctorRating(@PathVariable Long doctorId) {
        List<DoctorReview> reviews = doctorReviewRepository.findByDoctor_Id(doctorId);
        int reviewCount = reviews.size();
        Double rating = reviewCount == 0 ? null :
                Math.round(reviews.stream().mapToInt(DoctorReview::getRating).average().orElse(0.0) * 10.0) / 10.0;
        return ResponseEntity.ok(DoctorRatingDTO.builder()
                .doctorId(doctorId)
                .rating(rating)
                .reviewCount(reviewCount)
                .build());
    }
}
