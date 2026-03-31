package io.soyoung.addashboard.service;

import io.soyoung.addashboard.client.MetaApiClient;
import io.soyoung.addashboard.client.MetaApiClient.InsightData;
import io.soyoung.addashboard.dto.SyncResultResponse;
import io.soyoung.addashboard.entity.AdInsightRaw;
import io.soyoung.addashboard.repository.AdInsightRawRepository;
import jakarta.annotation.PostConstruct;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Meta 광고 성과 데이터 동기화 서비스.
 * Meta Graph API에서 조회한 데이터를 ad_insights_raw 테이블에 적재한다.
 * 동일 날짜/ID가 존재하면 Update, 없으면 Insert하는 Upsert 방식으로 동작한다.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SyncService {

    private final MetaApiClient metaApiClient;
    private final AdInsightRawRepository adInsightRawRepository;

    @Value("${meta.api.ad-account-id}")
    private String adAccountId;

    @PostConstruct
    void validateConfig() {
        if (adAccountId == null || adAccountId.isBlank()) {
            log.warn("meta.api.ad-account-id가 설정되지 않았습니다. 동기화 기능이 동작하지 않습니다.");
        }
    }

    /**
     * 지정된 날짜 범위의 Meta 광고 성과 데이터를 동기화한다.
     * 외부 API 호출은 트랜잭션 밖에서, DB 적재는 트랜잭션 안에서 수행한다.
     *
     * @param since 동기화 시작일
     * @param until 동기화 종료일
     * @return 동기화 결과 (상태, 건수, 메시지)
     */
    public SyncResultResponse syncInsights(LocalDate since, LocalDate until) {
        try {
            // 외부 API 호출 (트랜잭션 밖)
            List<InsightData> insights = metaApiClient.fetchInsights(adAccountId, since, until);

            // DB 적재 (트랜잭션 안)
            int syncedCount = saveInsights(insights);

            log.info("Meta 동기화 완료: {}건 처리 (기간: {} ~ {})", syncedCount, since, until);

            return SyncResultResponse.builder()
                .status("SUCCESS")
                .syncedCount(syncedCount)
                .message(since + " ~ " + until + " 기간의 데이터 " + syncedCount + "건 동기화 완료")
                .build();
        } catch (Exception e) {
            log.error("Meta 동기화 실패: {}", e.getMessage(), e);

            return SyncResultResponse.builder()
                .status("FAILED")
                .syncedCount(0)
                .message("동기화 실패: " + e.getMessage())
                .build();
        }
    }

    @Transactional
    protected int saveInsights(List<InsightData> insights) {
        int count = 0;
        for (InsightData data : insights) {
            upsertInsight(data);
            count++;
        }
        return count;
    }

    /**
     * 동일 metaId + logDate 조합이 존재하면 Update, 없으면 Insert한다.
     */
    private void upsertInsight(InsightData data) {
        adInsightRawRepository.findByMetaIdAndLogDate(data.metaId(), data.logDate())
            .ifPresentOrElse(
                existing -> existing.updateMetrics(
                    data.spend(), data.impressions(), data.clicks(), data.reach()),
                () -> adInsightRawRepository.save(AdInsightRaw.builder()
                    .metaId(data.metaId())
                    .logDate(data.logDate())
                    .spend(data.spend())
                    .impressions(data.impressions())
                    .clicks(data.clicks())
                    .reach(data.reach())
                    .build())
            );
    }
}
