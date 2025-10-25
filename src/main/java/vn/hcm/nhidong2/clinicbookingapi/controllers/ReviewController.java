package vn.hcm.nhidong2.clinicbookingapi.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import vn.hcm.nhidong2.clinicbookingapi.dtos.ReviewRequestDTO;
import vn.hcm.nhidong2.clinicbookingapi.dtos.ReviewResponseDTO;
import vn.hcm.nhidong2.clinicbookingapi.models.DoctorReview;
import vn.hcm.nhidong2.clinicbookingapi.services.ReviewService;

import java.util.Optional;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
@Tag(name = "Doctor Review API", description = "Bệnh nhân đánh giá bác sĩ sau khám")
@SecurityRequirement(name = "bearerAuth")
public class ReviewController {

    private final ReviewService reviewService;

    @Operation(summary = "Lấy đánh giá theo bệnh án", description = "Trả về đánh giá nếu bệnh nhân đã đánh giá bệnh án này")
    @ApiResponse(responseCode = "200", description = "Có đánh giá", content = @Content(schema = @Schema(implementation = ReviewResponseDTO.class)))
    @ApiResponse(responseCode = "404", description = "Chưa có đánh giá")
    @GetMapping("/medical-records/{recordId}")
    @PreAuthorize("hasAuthority('ROLE_PATIENT')")
    public ResponseEntity<ReviewResponseDTO> getReview(@PathVariable Long recordId) {
        Optional<DoctorReview> reviewOpt = reviewService.getReviewByMedicalRecordId(recordId);
        return reviewOpt
                .map(r -> ResponseEntity.ok(ReviewResponseDTO.fromEntity(r)))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @Operation(summary = "Gửi/Chỉnh sửa đánh giá cho bệnh án",
            description = "Chỉ bệnh nhân của lịch hẹn sau khi đã hoàn tất mới có thể đánh giá.")
    @ApiResponse(responseCode = "200", description = "Gửi đánh giá thành công")
    @PostMapping("/medical-records/{recordId}")
    @PreAuthorize("hasAuthority('ROLE_PATIENT')")
    public ResponseEntity<ReviewResponseDTO> submitReview(@PathVariable Long recordId,
                                                          @Valid @RequestBody ReviewRequestDTO request) {
        DoctorReview saved = reviewService.submitOrUpdateReview(recordId, request);
        return ResponseEntity.ok(ReviewResponseDTO.fromEntity(saved));
    }
}