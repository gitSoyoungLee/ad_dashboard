package io.soyoung.addashboard.dto;

import java.util.List;
import lombok.Builder;
import lombok.Getter;

/**
 * 가입자 목록 조회 시 사용
 */
@Getter
@Builder
public class UserListResponse {

    private List<UserDto> users;
}
