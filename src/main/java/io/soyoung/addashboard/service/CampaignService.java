package io.soyoung.addashboard.service;

import io.soyoung.addashboard.dto.CampaignSearchRequest;
import io.soyoung.addashboard.dto.CampaignStatResponse;
import io.soyoung.addashboard.entity.AdCategory;
import io.soyoung.addashboard.entity.AdEntity;
import io.soyoung.addashboard.entity.EntityType;
import io.soyoung.addashboard.repository.AdEntityRepository;
import io.soyoung.addashboard.repository.AdInsightRawRepository;
import io.soyoung.addashboard.repository.ConversionRepository;
import io.soyoung.addashboard.repository.UserRepository;
import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 캠페인별 상세 성과 분석 서비스.
 * Meta 캠페인 데이터와 내부 DB 전환(가입자) 데이터를 매칭하여
 * 캠페인별 실제 CPA를 산출하고, UTM 정합성을 검증한다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CampaignService {

    private final AdEntityRepository adEntityRepository;
    private final AdInsightRawRepository adInsightRawRepository;
    private final ConversionRepository conversionRepository;
    private final UserRepository userRepository;

    /**
     * 캠페인 목록을 조회하고 실제 성과 지표를 산출한다.
     * 캠페인 유형(type) 필터링, 정렬(sortBy) 기능을 지원한다.
     *
     * @param request 검색 조건 (시작일, 종료일, 캠페인 유형, 정렬 기준)
     * @return 캠페인별 성과 응답 목록
     */
    public List<CampaignStatResponse> getCampaigns(CampaignSearchRequest request) {
        // 캠페인 유형 필터링
        List<AdEntity> campaigns;
        if (request.getType() != null && !request.getType().isEmpty()) {
            try {
                AdCategory category = AdCategory.valueOf(request.getType());
                campaigns = adEntityRepository.findAllByEntityTypeAndAdCategory(
                    EntityType.CAMPAIGN, category);
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException(
                    "유효하지 않은 캠페인 유형입니다: " + request.getType());
            }
        } else {
            campaigns = adEntityRepository.findAllByEntityType(EntityType.CAMPAIGN);
        }

        List<CampaignStatResponse> results = campaigns.stream()
            .map(campaign -> buildCampaignStat(campaign, request))
            .collect(Collectors.toList());

        // 정렬 적용
        return applySorting(results, request.getSortBy());
    }

    /**
     * UTM 파라미터와 Meta campaign ID 간 정합성을 검증한다.
     * 내부 DB에 UTM 캠페인으로 유입된 가입자 수와 Meta 전환 수를 비교하여
     * 불일치 여부를 확인한다.
     *
     * @param metaCampaignId Meta 캠페인 ID
     * @return UTM 경유 가입자 수
     */
    public long getUtmMatchedUserCount(String metaCampaignId) {
        return userRepository.countByUtmCampaign(metaCampaignId);
    }

    private CampaignStatResponse buildCampaignStat(AdEntity campaign,
        CampaignSearchRequest request) {
        String metaId = campaign.getMetaId();

        // 기간 내 캠페인 하위 광고들의 metaId 수집
        List<String> adMetaIds = adEntityRepository.findAllByParentMetaId(metaId).stream()
            .map(AdEntity::getMetaId)
            .collect(Collectors.toList());
        adMetaIds.add(metaId);

        // 기간 내 지출액 합산
        BigDecimal spend = adInsightRawRepository.sumSpendByMetaIdsAndDateBetween(
            adMetaIds, request.getStartDate(), request.getEndDate());

        // 기간 내 클릭 수/노출 수 합산 (단일 쿼리)
        Object[] clicksAndImpressions = adInsightRawRepository
            .sumClicksAndImpressionsByMetaIdsAndDateBetween(
                adMetaIds, request.getStartDate(), request.getEndDate());
        long totalClicks = ((Number) clicksAndImpressions[0]).longValue();
        long totalImpressions = ((Number) clicksAndImpressions[1]).longValue();

        // CTR 계산 (노출 0이면 0.0)
        double ctr = totalImpressions > 0
            ? (double) totalClicks / totalImpressions * 100 : 0.0;

        // 내부 DB 전환 수 (Conversion 테이블 기반)
        int resultCount = (int) conversionRepository.countByCampaignIdAndCreatedAtBetween(
            metaId,
            request.getStartDate().atStartOfDay(),
            request.getEndDate().atTime(LocalTime.MAX));

        // 실제 CPA 산출 (전환 0이면 null)
        Integer actualCpa = resultCount > 0
            ? spend.divide(BigDecimal.valueOf(resultCount), 0,
                java.math.RoundingMode.HALF_UP).intValue()
            : null;

        return CampaignStatResponse.builder()
            .campaignId(metaId)
            .campaignName(campaign.getName())
            .campaignType(campaign.getAdCategory().name())
            .spend(spend.longValue())
            .clicks(totalClicks)
            .ctr(Math.round(ctr * 100.0) / 100.0)
            .resultCount(resultCount)
            .actualCpa(actualCpa)
            .build();
    }

    private List<CampaignStatResponse> applySorting(List<CampaignStatResponse> results,
        String sortBy) {
        if (sortBy == null || sortBy.isEmpty()) {
            return results;
        }

        Comparator<CampaignStatResponse> comparator = switch (sortBy) {
            case "spend" -> Comparator.comparing(CampaignStatResponse::getSpend,
                Comparator.nullsLast(Comparator.reverseOrder()));
            case "clicks" -> Comparator.comparing(CampaignStatResponse::getClicks,
                Comparator.nullsLast(Comparator.reverseOrder()));
            case "ctr" -> Comparator.comparing(CampaignStatResponse::getCtr,
                Comparator.nullsLast(Comparator.reverseOrder()));
            case "resultCount" -> Comparator.comparing(CampaignStatResponse::getResultCount,
                Comparator.nullsLast(Comparator.reverseOrder()));
            case "actualCpa" -> Comparator.comparing(CampaignStatResponse::getActualCpa,
                Comparator.nullsLast(Comparator.naturalOrder()));
            default -> null;
        };

        if (comparator != null) {
            results.sort(comparator);
        }
        return results;
    }
}
