package io.soyoung.addashboard.controller;

import io.soyoung.addashboard.dto.UserDto;
import io.soyoung.addashboard.dto.UserListResponse;
import io.soyoung.addashboard.entity.User;
import io.soyoung.addashboard.repository.UserRepository;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 가입자 관리 API 컨트롤러.
 * UTM 캠페인별 실제 가입 리스트를 조회한다.
 */
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;

    /**
     * 가입자 목록을 조회한다.
     * utmCampaign 파라미터가 있으면 해당 캠페인 경유 가입자만 필터링한다.
     *
     * @param utmCampaign UTM 캠페인 값 (선택)
     * @return 가입자 목록 응답
     */
    @GetMapping
    public ResponseEntity<UserListResponse> getUsers(
        @RequestParam(required = false) String utmCampaign) {
        List<User> users = (utmCampaign != null && !utmCampaign.isEmpty())
            ? userRepository.findAllByUtmCampaign(utmCampaign)
            : userRepository.findAll();

        List<UserDto> userDtos = users.stream()
            .map(user -> UserDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .utmSource(user.getUtmSource())
                .utmCampaign(user.getUtmCampaign())
                .createdAt(user.getCreatedAt())
                .build())
            .collect(Collectors.toList());

        return ResponseEntity.ok(UserListResponse.builder().users(userDtos).build());
    }
}
