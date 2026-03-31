package io.soyoung.addashboard.controller;

import io.soyoung.addashboard.dto.MetaSyncRequest;
import io.soyoung.addashboard.dto.SyncResultResponse;
import io.soyoung.addashboard.service.SyncService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Meta 데이터 동기화 API 컨트롤러.
 * 수동 동기화 트리거를 제공한다.
 */
@RestController
@RequestMapping("/api/v1/sync")
@RequiredArgsConstructor
public class SyncController {

    private final SyncService syncService;

    /**
     * Meta 광고 성과 데이터를 수동으로 동기화한다.
     *
     * @param request 동기화 요청 (대상 날짜)
     * @return 동기화 결과
     */
    @PostMapping("/meta")
    public ResponseEntity<SyncResultResponse> syncMeta(@Valid @RequestBody MetaSyncRequest request) {
        return ResponseEntity.ok(syncService.syncInsights(
            request.getTargetDate(), request.getTargetDate()));
    }
}
