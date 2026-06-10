package io.soyoung.addashboard.dto;

import java.time.LocalDate;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

/**
 * AI 광고 성과 분석 결과를 담는 응답 객체.
 */
@Getter
@Builder
public class AiAnalysisResponse {

    private String overallDiagnosis;
    private List<AdDiagnosis> adDiagnoses;
    private List<String> actionItems;
    private LocalDate startDate;
    private LocalDate endDate;

    /**
     * 개별 광고 소재에 대한 AI 진단 결과.
     */
    @Getter
    @Builder
    public static class AdDiagnosis {

        private String adName;
        private String campaignName;
        private String diagnosis;
        private String suggestion;
    }
}
