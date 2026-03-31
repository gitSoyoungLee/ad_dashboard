package io.soyoung.addashboard.service;

import io.soyoung.addashboard.dto.AdStatResponse;
import io.soyoung.addashboard.entity.AdEntity;
import io.soyoung.addashboard.entity.AdInsightRaw;
import io.soyoung.addashboard.repository.AdEntityRepository;
import io.soyoung.addashboard.repository.AdInsightRawRepository;
import io.soyoung.addashboard.repository.ConversionRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 광고 소재별 상세 성과 분석 서비스.
 * 특정 캠페인 하위의 광고 소재(creative)별 CPA를 계산하고,
 * 지출 대비 전환이 없는 "좀비 광고"를 식별하는 데 활용된다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdService {

    private final AdEntityRepository adEntityRepository;
    private final AdInsightRawRepository adInsightRawRepository;
    private final ConversionRepository conversionRepository;

    /**
     * 특정 캠페인 하위의 광고 소재별 성과를 조회한다.
     * 소재별 노출, 지출, 클릭, 전환 수, CPA, CTR을 산출하며,
     * 전환이 0이고 지출이 있는 소재는 좀비 광고로 판별할 수 있다.
     *
     * @param campaignMetaId 상위 캠페인의 Meta ID
     * @param startDate      조회 시작일
     * @param endDate        조회 종료일
     * @return 소재별 성과 응답 목록
     */
    public List<AdStatResponse> getAdStats(String campaignMetaId, LocalDate startDate,
        LocalDate endDate) {
        // 캠페인 하위 광고 소재 조회
        List<AdEntity> ads = adEntityRepository.findAllByParentMetaId(campaignMetaId);

        return ads.stream()
            .map(ad -> buildAdStat(ad, startDate, endDate))
            .collect(Collectors.toList());
    }

    private AdStatResponse buildAdStat(AdEntity ad, LocalDate startDate, LocalDate endDate) {
        String metaId = ad.getMetaId();

        // 기간 내 소재별 성과 데이터 집계
        List<AdInsightRaw> insights = adInsightRawRepository.findAllByMetaIdAndLogDateBetween(
            metaId, startDate, endDate);

        long totalImpressions = 0;
        long totalSpend = 0;
        long totalClicks = 0;

        for (AdInsightRaw insight : insights) {
            totalImpressions += insight.getImpressions() != null ? insight.getImpressions() : 0;
            totalSpend += insight.getSpend() != null ? insight.getSpend().longValue() : 0;
            totalClicks += insight.getClicks() != null ? insight.getClicks() : 0;
        }

        // 소재 및 하위 엔티티의 전환 수를 함께 집계
        List<String> targetIds = new java.util.ArrayList<>();
        targetIds.add(metaId);
        adEntityRepository.findAllByParentMetaId(metaId).stream()
            .map(AdEntity::getMetaId)
            .forEach(targetIds::add);

        int conversions = (int) conversionRepository.countByCampaignIdInAndCreatedAtBetween(
            targetIds,
            startDate.atStartOfDay(),
            endDate.atTime(LocalTime.MAX));

        // 소재별 CPA 산출 (전환 0이면 null → 좀비 광고 판별 근거)
        Integer cpa = conversions > 0
            ? (int) (totalSpend / conversions)
            : null;

        // CTR 계산 (노출 0이면 0.0)
        double ctr = totalImpressions > 0
            ? (double) totalClicks / totalImpressions * 100 : 0.0;

        return AdStatResponse.builder()
            .adId(metaId)
            .adName(ad.getName())
            .impressions(totalImpressions)
            .spend(totalSpend)
            .conversions(conversions)
            .cpa(cpa)
            .ctr(Math.round(ctr * 100.0) / 100.0)
            .build();
    }
}
