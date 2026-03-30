package io.soyoung.addashboard.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * 리드의 상태를 변경할 때 status 값만 전달받는 객체.
 */
@Getter
@Setter
public class LeadStatusUpdateRequest {

    private String status;
}
