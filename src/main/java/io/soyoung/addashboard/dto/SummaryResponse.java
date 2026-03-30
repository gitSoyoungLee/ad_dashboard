package io.soyoung.addashboard.dto;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

/**
 * 전체 광고 지출액, 가입자 수, 통합 CAC 등을 담는 핵심 응답 객체.
 */
@Getter
@Builder
public class SummaryResponse {

    private Long totalSpend;
    private Long totalInbound;
    private Integer userCount;
    private Integer validLeadCount;
    private Integer totalCac;
    private Integer leadCpa;
    private LocalDateTime syncedAt;
}
