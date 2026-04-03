# Meta API URI 템플릿 및 Content-Type 호환성 문제

## 관련 이슈

- #13 데이터 동기화 파이프라인 및 관리 API 구현

## 증상

Meta Graph API를 호출할 때 두 가지 문제가 동시에 발생했다.

1. **URI 템플릿 충돌**: `time_range` 파라미터의 JSON 값 `{"since":"...","until":"..."}`에 포함된 중괄호(`{}`)를 Spring의 `UriBuilder`가 URI 변수 플레이스홀더로 오인식하여 요청 URL이 깨졌다.
2. **Content-Type 파싱 실패**: Meta API가 응답 Content-Type을 `application/json`이 아닌 `text/javascript`로 반환하여, Spring의 기본 `JacksonJsonHttpMessageConverter`가 응답을 역직렬화하지 못했다.

## 원인

### URI 템플릿 충돌

Spring의 `UriBuilder.queryParam()`은 내부적으로 URI 템플릿을 처리한다. `{`와 `}`를 만나면 변수 바인딩 대상으로 간주하기 때문에, JSON 문자열 안의 중괄호도 변수로 해석되어 잘못된 URL이 생성되었다.

```java
// 문제 코드 - UriBuilder가 JSON 중괄호를 URI 변수로 해석
uriBuilder
    .queryParam("time_range", "{\"since\":\"" + since + "\",\"until\":\"" + until + "\"}")
    .build(adAccountId)
```

### Content-Type 호환성

Meta Graph API의 일부 엔드포인트는 역사적인 이유로 `text/javascript` Content-Type을 반환한다. Spring의 기본 `JacksonJsonHttpMessageConverter`는 `application/json`만 지원하므로, 정상적인 JSON 데이터임에도 파싱을 거부했다.

## 해결

### URI 문제: 문자열 기반 URI 템플릿으로 전환

`UriBuilder` 대신 문자열 템플릿과 위치 기반 변수를 사용하여, JSON 값이 URI 변수로 해석되는 것을 방지했다.

```java
// 수정 후 - 문자열 템플릿 + 위치 기반 변수
String timeRange = "{\"since\":\"" + since + "\",\"until\":\"" + until + "\"}";

restClient.get()
    .uri("/{adAccountId}/insights?access_token={token}&fields={fields}"
            + "&time_range={timeRange}&time_increment=1&level=ad&limit=500",
        adAccountId, accessToken,
        "campaign_id,spend,impressions,clicks,reach", timeRange)
    .retrieve()
    .body(new ParameterizedTypeReference<>() {});
```

### Content-Type 문제: 커스텀 MessageConverter 등록

`JacksonJsonHttpMessageConverter`가 `text/javascript`도 처리하도록 설정하고, RestClient에 우선순위로 등록했다.

```java
JacksonJsonHttpMessageConverter converter = new JacksonJsonHttpMessageConverter();
converter.setSupportedMediaTypes(List.of(
    MediaType.APPLICATION_JSON,
    new MediaType("text", "javascript")));

this.restClient = RestClient.builder()
    .baseUrl(baseUrl)
    .messageConverters(converters -> converters.add(0, converter))
    .build();
```

## 배운 점

- **Spring UriBuilder는 중괄호에 민감하다**: JSON처럼 `{}`를 포함하는 쿼리 파라미터는 `UriBuilder.queryParam()` 대신 문자열 URI 템플릿을 사용해야 안전하다.
- **외부 API의 Content-Type을 신뢰하지 말자**: 표준을 따르지 않는 API가 많으므로, 연동 초기에 실제 응답 헤더를 확인하고 MessageConverter를 적절히 설정해야 한다.
- **두 문제가 동시에 발생하면 디버깅이 어렵다**: URI가 잘못되면 요청 자체가 실패하고, Content-Type 문제는 응답 단계에서 실패한다. 한 번에 하나씩 해결하며 각 단계를 확인하는 것이 중요하다.
