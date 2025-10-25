package vn.hcm.nhidong2.clinicbookingapi.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "Điểm trung bình và tổng số lượt đánh giá của bác sĩ")
public class DoctorRatingDTO {
    @Schema(description = "ID của bác sĩ", example = "1")
    private Long doctorId;

    @Schema(description = "Điểm trung bình (1 chữ số thập phân). Null nếu chưa có đánh giá", example = "4.5")
    private Double rating;

    @Schema(description = "Tổng số lượt đánh giá", example = "12")
    private Integer reviewCount;
}