package io.soyoung.addashboard.scheduler;

import io.soyoung.addashboard.dto.SyncResultResponse;
import io.soyoung.addashboard.service.SyncService;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Meta 광고 데이터 자동 동기화 스케줄러.
 * 매일 한국 시간 오전 10시, 오후 4시에 실행된다.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class MetaSyncScheduler {

    private final SyncService syncService;

    /**
     * 오전 10시(KST) 실행: 어제 데이터를 동기화한다.
     */
    @Scheduled(cron = "${sync.cron.yesterday}", zone = "Asia/Seoul")
    public void syncYesterday() {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        log.info("[스케줄러] 어제({}) 데이터 동기화 시작", yesterday);

        SyncResultResponse result = syncService.syncInsights(yesterday, yesterday);
        log.info("[스케줄러] 어제 데이터 동기화 완료 - status={}, syncedCount={}, message={}",
            result.getStatus(), result.getSyncedCount(), result.getMessage());
    }

    /**
     * 오후 4시(KST) 실행: 최근 7일 데이터를 동기화한다.
     */
    @Scheduled(cron = "${sync.cron.seven-days}", zone = "Asia/Seoul")
    public void syncLastSevenDays() {
        LocalDate today = LocalDate.now();
        LocalDate sevenDaysAgo = today.minusDays(7);
        log.info("[스케줄러] 최근 7일({} ~ {}) 데이터 동기화 시작", sevenDaysAgo, today);

        SyncResultResponse result = syncService.syncInsights(sevenDaysAgo, today);
        log.info("[스케줄러] 최근 7일 데이터 동기화 완료 - status={}, syncedCount={}, message={}",
            result.getStatus(), result.getSyncedCount(), result.getMessage());
    }
}
