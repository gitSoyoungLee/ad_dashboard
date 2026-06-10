package io.soyoung.addashboard.client;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/**
 * Google Gemini API 호출 클라이언트. 프롬프트를 전송하고 텍스트 응답을 반환한다.
 */
@Component
public class GeminiApiClient {

    private final RestClient restClient;
    private final String apiKey;
    private final String model;

    public GeminiApiClient(
        @Value("${gemini.api.key}") String apiKey,
        @Value("${gemini.api.model:gemini-2.0-flash}") String model) {

        // 연결 타임아웃 5초, 읽기 타임아웃 30초
        JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory();
        factory.setReadTimeout(Duration.ofSeconds(30));

        this.restClient = RestClient.builder()
            .baseUrl("https://generativelanguage.googleapis.com/v1beta")
            .requestFactory(factory)
            .build();
        this.apiKey = apiKey;
        this.model = model;
    }

    /**
     * Gemini API에 프롬프트를 전송하고 텍스트 응답을 반환한다.
     *
     * @param prompt 전송할 프롬프트
     * @return Gemini가 생성한 텍스트 응답
     */
    public String generate(String prompt) {
        Map<String, Object> requestBody = Map.of(
            "contents", List.of(
                Map.of("parts", List.of(
                    Map.of("text", prompt)
                ))
            )
        );

        Map<String, Object> response = restClient.post()
            .uri("/models/{model}:generateContent", model)
            .header("Content-Type", "application/json")
            .header("X-goog-api-key", apiKey)
            .body(requestBody)
            .retrieve()
            .body(new ParameterizedTypeReference<>() {
            });

        return extractText(response);
    }

    // Object -> List<Map<String, Object>> 로 캐스팅할 때 컴파일러가 경고
    // Gemini API 응답 구조는 이미 정해져있으므로 unchecked 경고 스킵하기 위해 어노테이션 사용
    @SuppressWarnings("unchecked")
    private String extractText(Map<String, Object> response) {
        // candidates = Gemini가 생성한 응답 후보 목록
        if (response == null || !response.containsKey("candidates")) {
            return "";
        }

        List<Map<String, Object>> candidates =
            (List<Map<String, Object>>) response.get("candidates");
        if (candidates.isEmpty()) {
            return "";
        }

        // parts = 하나의 응답이 여러 파트(텍스트, 이미지 등)로 구성될 수 있음
        // 텍스트만 사용하므로 첫 번째 part의 text만 추출

        Map<String, Object> content =
            (Map<String, Object>) candidates.get(0).get("content");
        if (content == null || !content.containsKey("parts")) {
            return "";
        }

        List<Map<String, Object>> parts =
            (List<Map<String, Object>>) content.get("parts");
        if (parts.isEmpty()) {
            return "";
        }

        return parts.get(0).getOrDefault("text", "").toString();
    }
}
