package io.soyoung.addashboard.dto;

import lombok.Builder;
import lombok.Getter;

/**
 * 단건 리드 정보
 */
@Getter
@Builder
public class LeadDto {

    private Long leadId;
    private String customerName;
    private String email;
    private String status;
    private Boolean isQualified;
}
