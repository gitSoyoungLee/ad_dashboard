# 루프 내 개별 DB 쿼리로 인한 N+1 문제

## 관련 이슈

- #10 DashboardService 지표 산출 로직 구현
- #12 캠페인 성과 분석 API 구현
- #16, #17 PR 리뷰 반영

## 증상

대시보드 API 응답 시간이 비정상적으로 느렸다. 특히 최근 30일 트렌드 조회 시 체감할 수 있을 정도의 지연이 발생했다.

## 원인

### 사례 1: 캠페인 클릭/노출 집계 (CampaignService)

캠페인에 속한 광고의 metaId 목록을 순회하며 **개별적으로 DB를 조회**하고 있었다.

```java
// 문제 코드 - metaId마다 개별 쿼리 실행 (N+1)
for (String id : adMetaIds) {
    var insights = adInsightRawRepository.findAllByMetaIdAndLogDateBetween(
        id, request.getStartDate(), request.getEndDate());
    for (var insight : insights) {
        totalClicks += insight.getClicks() != null ? insight.getClicks() : 0;
        totalImpressions += insight.getImpressions() != null ? insight.getImpressions() : 0;
    }
}
```

광고가 10개면 10번, 100개면 100번의 쿼리가 실행되는 전형적인 N+1 문제였다.

### 사례 2: 일별 가입자 수 집계 (DashboardService)

최근 30일 트렌드를 만들기 위해 **날짜마다 개별 쿼리**를 실행하고 있었다.

```java
// 문제 코드 - 30일 동안 매일 개별 쿼리 (30회 DB 호출)
for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
    LocalDateTime dayStart = date.atStartOfDay();
    LocalDateTime dayEnd = date.atTime(LocalTime.MAX);
    int dailyUsers = (int) userRepository.countByCreatedAtBetween(dayStart, dayEnd);
    userCountData.add(dailyUsers);
}
```

## 해결

### 사례 1: IN 절을 사용한 배치 집계 쿼리

metaId 목록 전체를 `IN` 절로 한 번에 넘기는 집계 쿼리로 대체했다.

```java
// 수정 후 - 단일 배치 쿼리
@Query("SELECT COALESCE(SUM(a.clicks), 0) FROM AdInsightRaw a "
    + "WHERE a.metaId IN :metaIds AND a.logDate BETWEEN :start AND :end")
Long sumClicksByMetaIdsAndDateBetween(@Param("metaIds") List<String> metaIds,
    @Param("start") LocalDate start, @Param("end") LocalDate end);
```

### 사례 2: GROUP BY를 사용한 일별 집계 쿼리

30번의 개별 쿼리를 `GROUP BY`로 한 번에 처리하는 JPQL로 교체했다.

```java
// 수정 후 - GROUP BY로 일별 집계를 단일 쿼리로 처리
@Query("SELECT CAST(u.createdAt AS LocalDate), COUNT(u) FROM User u "
    + "WHERE u.createdAt BETWEEN :start AND :end "
    + "GROUP BY CAST(u.createdAt AS LocalDate) "
    + "ORDER BY CAST(u.createdAt AS LocalDate)")
List<Object[]> countDailyUsersBetween(@Param("start") LocalDateTime start,
    @Param("end") LocalDateTime end);
```

서비스 코드에서는 결과를 `Map<LocalDate, Long>`으로 변환한 후 날짜별로 조회했다.

```java
Map<LocalDate, Long> userCountMap = dailyUsers.stream()
    .collect(Collectors.toMap(
        row -> (LocalDate) row[0],
        row -> (Long) row[1]
    ));

// 루프에서는 Map 조회만 수행 (DB 호출 없음)
userCountData.add(userCountMap.getOrDefault(date, 0L).intValue());
```

## 배운 점

- **루프 안에서 DB 호출이 보이면 N+1을 의심하자**: `for` 루프 내부에 Repository 메서드 호출이 있다면, 거의 확실하게 배치 쿼리로 개선할 수 있다.
- **`IN` 절과 `GROUP BY`는 N+1의 가장 기본적인 해결책이다**: 목록 기반 조회는 `WHERE id IN :ids`로, 날짜/카테고리 기반 반복 조회는 `GROUP BY`로 단일 쿼리 전환이 가능하다.
- **PR 리뷰에서 잡아낸 문제다**: 코드가 동작은 하지만 성능이 나쁜 패턴은 리뷰 과정에서 발견되는 경우가 많다. 코드 리뷰의 가치를 다시 한번 확인했다.
- **쿼리 결과를 Map으로 변환하는 패턴을 기억하자**: 배치 쿼리 결과를 `Map<Key, Value>`로 만들어두면, 이후 루프에서 `O(1)` 조회가 가능하여 코드도 깔끔해진다.
