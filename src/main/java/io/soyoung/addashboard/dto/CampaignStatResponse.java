package io.soyoung.addashboard.dto;

import lombok.Builder;
import lombok.Getter;

/**
 * 캠페인별 이름, 지출액, 실제 전환 수, Actual CPA를 담는 객체.
 */
@Getter
@Builder
public class CampaignStatResponse {

    private String campaignId;
    private String campaignName;
    private String campaignType;
    private Long spend;
    private Long clicks;
    private Double ctr;
    private Integer resultCount;
    private Integer actualCpa;
}
