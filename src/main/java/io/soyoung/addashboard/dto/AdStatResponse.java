package io.soyoung.addashboard.dto;

import lombok.Builder;
import lombok.Getter;

/**
 * 특정 캠페인 하위의 광고 소재별 성과(노출, 지출, 소재별 CPA)를 담는 객체.
 */
@Getter
@Builder
public class AdStatResponse {

    private String adId;
    private String adName;
    private Long impressions;
    private Long spend;
    private Integer conversions;
    private Integer cpa;
    private Double ctr;
}
