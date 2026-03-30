package io.soyoung.addashboard.dto;

import java.util.List;
import lombok.Builder;
import lombok.Getter;

/**
 * 리드 정보와 상담 상태(isQualified)를 전달하는 객체.
 */
@Getter
@Builder
public class LeadListResponse {

    private List<LeadDto> leads;
}
