package io.soyoung.addashboard.controller;

import io.soyoung.addashboard.dto.AdStatResponse;
import io.soyoung.addashboard.dto.CampaignSearchRequest;
import io.soyoung.addashboard.dto.CampaignStatResponse;
import io.soyoung.addashboard.dto.SummaryResponse;
import io.soyoung.addashboard.dto.TrendResponse;
import io.soyoung.addashboard.service.AdService;
import io.soyoung.addashboard.service.CampaignService;
import io.soyoung.addashboard.service.DashboardService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 대시보드 및 성과 분석 API 컨트롤러.
 * 프론트엔드에서 호출 가능한 REST API 엔드포인트를 제공한다.
 */
@RestController
@RequestMapping("/api/v1/stats")
@RequiredArgsConstructor
public class StatsController {

    private final DashboardService dashboardService;
    private final CampaignService campaignService;
    private final AdService adService;

    /**
     * 대시보드 종합 요약 통계를 조회한다.
     *
     * @param startDate 조회 시작일
     * @param endDate   조회 종료일
     * @return 종합 요약 응답
     */
    @GetMapping("/summary")
    public ResponseEntity<SummaryResponse> getSummary(
        @RequestParam @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
        @RequestParam @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(dashboardService.getSummary(startDate, endDate));
    }

    /**
     * 최근 30일간 일별 추이 데이터를 조회한다.
     *
     * @param endDate 조회 기준 종료일
     * @return 일별 추이 응답
     */
    @GetMapping("/trends")
    public ResponseEntity<TrendResponse> getTrends(
        @RequestParam @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(dashboardService.getTrends(endDate));
    }

    /**
     * 캠페인 목록 및 성과를 조회한다.
     * 캠페인 유형 필터링과 정렬을 지원한다.
     *
     * @param request 검색 조건 (시작일, 종료일, 유형, 정렬 기준)
     * @return 캠페인별 성과 목록
     */
    @GetMapping("/campaigns")
    public ResponseEntity<List<CampaignStatResponse>> getCampaigns(
        @Valid CampaignSearchRequest request) {
        return ResponseEntity.ok(campaignService.getCampaigns(request));
    }

    /**
     * 특정 캠페인 하위의 광고 소재별 성과를 조회한다.
     *
     * @param campaignId 캠페인 Meta ID
     * @param startDate  조회 시작일
     * @param endDate    조회 종료일
     * @return 소재별 성과 목록
     */
    @GetMapping("/campaigns/{campaignId}/ads")
    public ResponseEntity<List<AdStatResponse>> getAdStats(
        @PathVariable String campaignId,
        @RequestParam @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
        @RequestParam @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(adService.getAdStats(campaignId, startDate, endDate));
    }
}
