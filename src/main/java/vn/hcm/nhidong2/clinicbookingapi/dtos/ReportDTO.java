package vn.hcm.nhidong2.clinicbookingapi.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "Đối tượng dữ liệu cho Báo cáo thống kê")
public class ReportDTO {
    @Schema(description = "Tên hạng mục thống kê", example = "COMPLETED")
    private String name;

    @Schema(description = "Giá trị/Số lượng tương ứng", example = "150")
    private Long count;

    @Schema(description = "Thông tin thêm (ví dụ: Tên bác sĩ, Chuyên khoa)", example = "Khoa Hô hấp")
    private String additionalInfo;
}