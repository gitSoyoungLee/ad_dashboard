package io.soyoung.addashboard.dto;

import java.util.List;
import lombok.Builder;
import lombok.Getter;

/**
 * 시계열 차트를 그리기 위한 날짜별 배열(List<Long>)들을 모아둔 객체.
 */
@Getter
@Builder
public class TrendResponse {

    private List<String> labels;
    private List<Long> spendData;
    private List<Long> impressionsData;
    private List<Long> clicksData;
    private List<Integer> userCountData;
}
