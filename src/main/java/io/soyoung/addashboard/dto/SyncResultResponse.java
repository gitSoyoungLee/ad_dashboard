package io.soyoung.addashboard.dto;

import lombok.Builder;
import lombok.Getter;

/**
 * 동기화 성공 여부, 업데이트된 건수, 메시지를 담는 객체
 */
@Getter
@Builder
public class SyncResultResponse {

    private String status;
    private Integer syncedCount;
    private String message;
}
