package io.soyoung.addashboard.dto;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

/**
 * 단건 유저 정보
 */
@Getter
@Builder
public class UserDto {

    private Long id;
    private String email;
    private String name;
    private String utmSource;
    private String utmCampaign;
    private LocalDateTime createdAt;
}
