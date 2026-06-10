package io.soyoung.addashboard.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.soyoung.addashboard.client.GeminiApiClient;
import io.soyoung.addashboard.dto.AiAnalysisResponse;
import io.soyoung.addashboard.dto.AiAnalysisResponse.AdDiagnosis;
import io.soyoung.addashboard.entity.AdEntity;
import io.soyoung.addashboard.entity.AdInsightRaw;
import io.soyoung.addashboard.entity.EntityType;
import io.soyoung.addashboard.repository.AdEntityRepository;
import io.soyoung.addashboard.repository.AdInsightRawRepository;
import io.soyoung.addashboard.repository.ConversionRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * AI 기반 광고 성과 분석 서비스. 최근 7일간의 소재별 성과 데이터를 수집하여 Gemini API로 종합 분석을 수행한다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AiAnalysisService {

    private final AdEntityRepository adEntityRepository;
    private final AdInsightRawRepository adInsightRawRepository;
    private final ConversionRepository conversionRepository;
    private final GeminiApiClient geminiApiClient;
    private final ObjectMapper objectMapper;

    /**
     * 최근 7일간의 광고 소재 성과를 AI로 분석한다.
     *
     * @return AI 분석 결과
     */
    public AiAnalysisResponse analyze() {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(6);

        String adDataText = collectAdData(startDate, endDate);
        String prompt = buildPrompt(adDataText, startDate, endDate);
        String response = geminiApiClient.generate(prompt);

        return parseResponse(response, startDate, endDate);
    }

    /**
     * 모든 캠페인의 소재별 성과 데이터를 텍스트로 수집한다.
     */
    private String collectAdData(LocalDate startDate, LocalDate endDate) {
        List<AdEntity> campaigns = adEntityRepository.findAllByEntityType(EntityType.CAMPAIGN);
        StringBuilder sb = new StringBuilder();

        for (AdEntity campaign : campaigns) {
            List<AdEntity> ads = adEntityRepository.findAllByParentMetaId(campaign.getMetaId());
            if (ads.isEmpty()) {
                continue;
            }

            sb.append("\n[캠페인: ").append(campaign.getName())
                .append(" (유형: ").append(campaign.getAdCategory()).append(")]\n");

            for (AdEntity ad : ads) {
                appendAdStats(sb, ad, campaign.getMetaId(), startDate, endDate);
            }
        }

        return sb.toString();
    }

    private void appendAdStats(StringBuilder sb, AdEntity ad, String campaignMetaId,
        LocalDate startDate, LocalDate endDate) {
        String metaId = ad.getMetaId();

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

        // 전환 수 집계
        List<String> targetIds = new ArrayList<>();
        targetIds.add(metaId);
        adEntityRepository.findAllByParentMetaId(metaId).stream()
            .map(AdEntity::getMetaId)
            .forEach(targetIds::add);

        int conversions = (int) conversionRepository.countByCampaignIdInAndCreatedAtBetween(
            targetIds, startDate.atStartOfDay(), endDate.atTime(LocalTime.MAX));

        double ctr = totalImpressions > 0
            ? (double) totalClicks / totalImpressions * 100 : 0.0;
        ctr = Math.round(ctr * 100.0) / 100.0;

        String cpa = conversions > 0
            ? BigDecimal.valueOf(totalSpend)
            .divide(BigDecimal.valueOf(conversions), 0, RoundingMode.HALF_UP)
            .toString() + "원"
            : "전환 없음";

        sb.append("  - 소재: ").append(ad.getName())
            .append(" | 지출: ").append(totalSpend).append("원")
            .append(" | 노출: ").append(totalImpressions)
            .append(" | 클릭: ").append(totalClicks)
            .append(" | CTR: ").append(ctr).append("%")
            .append(" | 전환: ").append(conversions)
            .append(" | CPA: ").append(cpa)
            .append("\n");
    }

    // TODO: 프롬프트 외부 파일(txt)로 분리하여 관리
    private String buildPrompt(String adDataText, LocalDate startDate, LocalDate endDate) {
        return """
            너는 디지털 광고 성과 분석 전문가야.
            아래는 %s ~ %s 기간의 광고 소재별 성과 데이터야.
            이 데이터를 분석하여 반드시 아래 JSON 형식으로만 응답해줘.
            JSON 외의 텍스트는 절대 포함하지 마.

            데이터:
            %s

            응답 JSON 형식:
            {
              "overallDiagnosis": "전체 광고 계정의 성과를 종합적으로 진단한 내용",
              "adDiagnoses": [
                {
                  "adName": "소재명",
                  "campaignName": "캠페인명",
                  "diagnosis": "해당 소재의 성과 진단",
                  "suggestion": "구체적인 개선 제안"
                }
              ],
              "actionItems": [
                "즉시 실행할 수 있는 구체적인 액션 아이템 1",
                "액션 아이템 2"
              ]
            }

            분석 시 다음 사항을 고려해줘:
            - CTR이 높지만 전환이 낮은 소재는 랜딩페이지 문제일 수 있음
            - 지출이 있지만 전환이 0인 소재는 좀비 광고로 즉시 중단 권장
            - CPA가 평균보다 현저히 높은 소재는 타겟팅 재검토 필요
            - 성과가 좋은 소재의 특징을 파악하여 신규 소재 제작에 활용 제안
            """.formatted(startDate, endDate, adDataText);
    }

    private AiAnalysisResponse parseResponse(String response, LocalDate startDate,
        LocalDate endDate) {
        try {
            // Gemini가 코드블록으로 감쌀 경우 제거
            String json = response.strip();
            if (json.startsWith("```")) {
                json = json.replaceFirst("```json\\s*", "").replaceFirst("```\\s*$", "");
            }

            JsonNode root = objectMapper.readTree(json);

            String overallDiagnosis = root.path("overallDiagnosis").asText("");

            List<AdDiagnosis> adDiagnoses = new ArrayList<>();
            for (JsonNode node : root.path("adDiagnoses")) {
                adDiagnoses.add(AdDiagnosis.builder()
                    .adName(node.path("adName").asText(""))
                    .campaignName(node.path("campaignName").asText(""))
                    .diagnosis(node.path("diagnosis").asText(""))
                    .suggestion(node.path("suggestion").asText(""))
                    .build());
            }

            List<String> actionItems = new ArrayList<>();
            for (JsonNode node : root.path("actionItems")) {
                actionItems.add(node.asText());
            }

            return AiAnalysisResponse.builder()
                .overallDiagnosis(overallDiagnosis)
                .adDiagnoses(adDiagnoses)
                .actionItems(actionItems)
                .startDate(startDate)
                .endDate(endDate)
                .build();

        } catch (JsonProcessingException e) {
            return AiAnalysisResponse.builder()
                .overallDiagnosis(response)
                .adDiagnoses(List.of())
                .actionItems(List.of())
                .startDate(startDate)
                .endDate(endDate)
                .build();
        }
    }
}
