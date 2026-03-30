package io.soyoung.addashboard.dto;

import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;

/**
 * (Request) 시작일, 종료일, 캠페인 유형(Enum) 등 필터 조건을 받는 객체.
 */
@Getter
@Setter
public class CampaignSearchRequest {

    private LocalDate startDate;
    private LocalDate endDate;
    private String type;
    private String sortBy;
}
