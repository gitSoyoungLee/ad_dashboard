package io.soyoung.addashboard.service;

import io.soyoung.addashboard.dto.SummaryResponse;
import io.soyoung.addashboard.dto.TrendResponse;
import io.soyoung.addashboard.entity.LeadStatus;
import io.soyoung.addashboard.repository.AdInsightRawRepository;
import io.soyoung.addashboard.repository.LeadRepository;
import io.soyoung.addashboard.repository.UserRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 대시보드 핵심 통계 연산 서비스.
 * 광고 지출 데이터와 내부 전환 지표를 결합하여 통합 CAC 등의 핵심 KPI를 산출한다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardService {

    private final AdInsightRawRepository adInsightRawRepository;
    private final UserRepository userRepository;
    private final LeadRepository leadRepository;

    /**
     * 대시보드 요약 통계를 조회한다.
     * 기간 내 총 광고 지출액, 가입자 수, 유효 리드 수를 집계하고
     * 통합 CAC(총 지출 / 총 유입)와 리드 CPA(총 지출 / 유효 리드)를 산출한다.
     *
     * @param startDate 조회 시작일
     * @param endDate   조회 종료일
     * @return 대시보드 요약 응답 객체
     */
    public SummaryResponse getSummary(LocalDate startDate, LocalDate endDate) {
        // 기간 내 총 광고 지출액 합산
        BigDecimal totalSpend = adInsightRawRepository.sumSpendBetween(startDate, endDate);

        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);

        // 기간 내 신규 가입자 수
        long userCount = userRepository.countByCreatedAtBetween(startDateTime, endDateTime);

        // 검증 완료된 유효 리드 수
        long validLeadCount = leadRepository.countByStatus(LeadStatus.VERIFIED);

        // 총 유입 수 = 가입자 + 유효 리드
        long totalInbound = userCount + validLeadCount;

        // 통합 CAC 산출 (유입이 0이면 null 처리하여 0 나눗셈 방지)
        Integer totalCac = null;
        if (totalInbound > 0) {
            totalCac = totalSpend.divide(BigDecimal.valueOf(totalInbound), 0,
                java.math.RoundingMode.HALF_UP).intValue();
        }

        // 리드 CPA 산출 (유효 리드가 0이면 null 처리)
        Integer leadCpa = null;
        if (validLeadCount > 0) {
            leadCpa = totalSpend.divide(BigDecimal.valueOf(validLeadCount), 0,
                java.math.RoundingMode.HALF_UP).intValue();
        }

        return SummaryResponse.builder()
            .totalSpend(totalSpend.longValue())
            .totalInbound(totalInbound)
            .userCount((int) userCount)
            .validLeadCount((int) validLeadCount)
            .totalCac(totalCac)
            .leadCpa(leadCpa)
            .syncedAt(LocalDateTime.now())
            .build();
    }

    /**
     * 최근 30일간 일별 추이 데이터를 생성한다.
     * 날짜별 광고 지출, 노출 수, 클릭 수, 신규 가입자 수를 집계하여
     * 시계열 차트용 응답 객체를 반환한다.
     *
     * @param endDate 조회 기준 종료일 (이 날짜로부터 과거 30일)
     * @return 일별 추이 응답 객체
     */
    public TrendResponse getTrends(LocalDate endDate) {
        LocalDate startDate = endDate.minusDays(29);

        // 기간 내 일별 지출/노출/클릭 집계 조회
        List<Object[]> dailyStats = adInsightRawRepository.findDailyStatsBetween(
            startDate, endDate);

        // 날짜를 키로 하는 Map으로 변환하여 빠른 조회 지원
        Map<LocalDate, Object[]> statsMap = dailyStats.stream()
            .collect(Collectors.toMap(
                row -> (LocalDate) row[0],
                row -> row
            ));

        List<String> labels = new ArrayList<>();
        List<Long> spendData = new ArrayList<>();
        List<Long> impressionsData = new ArrayList<>();
        List<Long> clicksData = new ArrayList<>();
        List<Integer> userCountData = new ArrayList<>();

        // 30일 전체를 순회하며, 데이터가 없는 날짜는 0으로 채움
        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            labels.add(date.toString());

            Object[] row = statsMap.get(date);
            if (row != null) {
                spendData.add(((BigDecimal) row[1]).longValue());
                impressionsData.add(((Number) row[2]).longValue());
                clicksData.add(((Number) row[3]).longValue());
            } else {
                spendData.add(0L);
                impressionsData.add(0L);
                clicksData.add(0L);
            }

            // 해당 날짜의 신규 가입자 수 조회
            LocalDateTime dayStart = date.atStartOfDay();
            LocalDateTime dayEnd = date.atTime(LocalTime.MAX);
            int dailyUsers = (int) userRepository.countByCreatedAtBetween(dayStart, dayEnd);
            userCountData.add(dailyUsers);
        }

        return TrendResponse.builder()
            .labels(labels)
            .spendData(spendData)
            .impressionsData(impressionsData)
            .clicksData(clicksData)
            .userCountData(userCountData)
            .build();
    }
}
