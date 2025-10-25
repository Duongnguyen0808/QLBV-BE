package vn.hcm.nhidong2.clinicbookingapi.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
@Schema(description = "Yêu cầu gửi đánh giá cho bác sĩ sau khám")
public class ReviewRequestDTO {
    @Min(1)
    @Max(5)
    @Schema(description = "Số sao (1-5)", example = "5")
    private int rating;

    @Schema(description = "Nhận xét thêm", example = "Bác sĩ tư vấn rất kỹ và thân thiện")
    private String comment;
}