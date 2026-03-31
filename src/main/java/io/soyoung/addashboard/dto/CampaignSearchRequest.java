package io.soyoung.addashboard.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;

/**
 * (Request) 시작일, 종료일, 캠페인 유형(Enum) 등 필터 조건을 받는 객체.
 */
@Getter
@Setter
public class CampaignSearchRequest {

    @NotNull(message = "시작일은 필수입니다")
    private LocalDate startDate;

    @NotNull(message = "종료일은 필수입니다")
    private LocalDate endDate;

    private String type;
    private String sortBy;
}
