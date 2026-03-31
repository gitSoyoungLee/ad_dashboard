package io.soyoung.addashboard.client;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/**
 * Meta Graph API(Insights) 호출 클라이언트.
 * 광고 계정의 일별 성과 데이터를 조회한다.
 */
@Component
public class MetaApiClient {

    private final RestClient restClient;
    private final String accessToken;

    public MetaApiClient(
        @Value("${meta.api.base-url:https://graph.facebook.com/v21.0}") String baseUrl,
        @Value("${meta.api.access-token:}") String accessToken) {
        this.restClient = RestClient.builder()
            .baseUrl(baseUrl)
            .build();
        this.accessToken = accessToken;
    }

    /**
     * 특정 광고 계정의 일별 Insights 데이터를 조회한다.
     *
     * @param adAccountId Meta 광고 계정 ID (act_XXXXXXXXX)
     * @param since       조회 시작일
     * @param until       조회 종료일
     * @return 일별 성과 데이터 목록
     */
    public List<InsightData> fetchInsights(String adAccountId, LocalDate since, LocalDate until) {
        Map<String, Object> response = restClient.get()
            .uri(uriBuilder -> uriBuilder
                .path("/{adAccountId}/insights")
                .queryParam("access_token", accessToken)
                .queryParam("fields", "campaign_id,spend,impressions,clicks,reach")
                .queryParam("time_range", "{\"since\":\"" + since + "\",\"until\":\"" + until + "\"}")
                .queryParam("time_increment", "1")
                .queryParam("level", "ad")
                .queryParam("limit", "500")
                .build(adAccountId))
            .retrieve()
            .body(new ParameterizedTypeReference<>() {});

        if (response == null || !response.containsKey("data")) {
            return List.of();
        }

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> data = (List<Map<String, Object>>) response.get("data");

        return data.stream()
            .map(this::toInsightData)
            .collect(Collectors.toList());
    }

    private InsightData toInsightData(Map<String, Object> raw) {
        return new InsightData(
            (String) raw.getOrDefault("campaign_id", ""),
            LocalDate.parse((String) raw.get("date_start")),
            new BigDecimal(raw.getOrDefault("spend", "0").toString()),
            Integer.parseInt(raw.getOrDefault("impressions", "0").toString()),
            Integer.parseInt(raw.getOrDefault("clicks", "0").toString()),
            Integer.parseInt(raw.getOrDefault("reach", "0").toString())
        );
    }

    /**
     * Meta API에서 반환된 일별 성과 데이터를 담는 레코드.
     */
    public record InsightData(
        String metaId,
        LocalDate logDate,
        BigDecimal spend,
        int impressions,
        int clicks,
        int reach
    ) {}
}
