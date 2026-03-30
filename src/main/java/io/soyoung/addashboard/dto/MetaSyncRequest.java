package io.soyoung.addashboard.dto;

import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;

/**
 * (Request) 동기화할 타겟 날짜(targetDate)를 전달받는 객체.
 */
@Getter
@Setter
public class MetaSyncRequest {

    private LocalDate targetDate;
    private String syncType;
}
