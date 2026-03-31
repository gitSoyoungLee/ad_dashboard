package io.soyoung.addashboard.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;

/**
 * (Request) 동기화할 날짜 범위를 전달받는 객체.
 */
@Getter
@Setter
public class MetaSyncRequest {

    @NotNull(message = "시작일은 필수입니다")
    private LocalDate startDate;

    @NotNull(message = "종료일은 필수입니다")
    private LocalDate endDate;

    private String syncType;
}
