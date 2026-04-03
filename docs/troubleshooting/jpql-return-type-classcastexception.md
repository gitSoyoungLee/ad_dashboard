# JPQL 다중 컬럼 집계 쿼리 ClassCastException

## 관련 이슈

- #12 대시보드 및 성과 분석 API 구현

## 증상

캠페인별 클릭 수와 노출 수를 집계하는 API 호출 시 `ClassCastException`이 발생했다.

```
java.lang.ClassCastException: [Ljava.lang.Object; cannot be cast to java.lang.Long
```

## 원인

JPQL에서 `SELECT SUM(a.clicks), SUM(a.impressions)`처럼 **다중 컬럼을 반환하는 집계 쿼리**는 JPA가 결과를 `Object[]` 배열로 반환한다. 그런데 서비스 코드에서 이 결과를 올바르게 캐스팅하지 못해 문제가 발생했다.

```java
// Repository - Object[]를 반환하는 JPQL
@Query("SELECT COALESCE(SUM(a.clicks), 0), COALESCE(SUM(a.impressions), 0) "
    + "FROM AdInsightRaw a "
    + "WHERE a.metaId IN :metaIds AND a.logDate BETWEEN :start AND :end")
Object[] sumClicksAndImpressionsByMetaIdsAndDateBetween(...);
```

```java
// Service - Object[] 원소를 Number로 캐스팅해야 하는데 직접 Long으로 사용 시도
long totalClicks = ((Number) clicksAndImpressions[0]).longValue();
long totalImpressions = ((Number) clicksAndImpressions[1]).longValue();
```

실제로는 `Object[]`의 캐스팅 자체가 혼란을 야기했고, JPA 구현체에 따라 반환 타입이 `Long`, `BigDecimal`, `BigInteger` 등으로 달라질 수 있어 타입 안전성이 보장되지 않았다.

## 해결

**다중 컬럼 반환 쿼리를 단일 값 반환 쿼리 2개로 분리**하여, 반환 타입을 `Long`으로 명확히 지정했다.

```java
// 수정 후 - 각각 Long을 직접 반환
@Query("SELECT COALESCE(SUM(a.clicks), 0) FROM AdInsightRaw a "
    + "WHERE a.metaId IN :metaIds AND a.logDate BETWEEN :start AND :end")
Long sumClicksByMetaIdsAndDateBetween(@Param("metaIds") List<String> metaIds,
    @Param("start") LocalDate start, @Param("end") LocalDate end);

@Query("SELECT COALESCE(SUM(a.impressions), 0) FROM AdInsightRaw a "
    + "WHERE a.metaId IN :metaIds AND a.logDate BETWEEN :start AND :end")
Long sumImpressionsByMetaIdsAndDateBetween(@Param("metaIds") List<String> metaIds,
    @Param("start") LocalDate start, @Param("end") LocalDate end);
```

```java
// Service - 타입 캐스팅 없이 직접 사용
long totalClicks = adInsightRawRepository.sumClicksByMetaIdsAndDateBetween(
    adMetaIds, request.getStartDate(), request.getEndDate());
long totalImpressions = adInsightRawRepository.sumImpressionsByMetaIdsAndDateBetween(
    adMetaIds, request.getStartDate(), request.getEndDate());
```

## 배운 점

- **JPQL 다중 컬럼 집계의 반환 타입은 `Object[]`이다**: 단일 컬럼이면 해당 타입으로 바로 반환되지만, 다중 컬럼은 반드시 `Object[]`로 받아야 한다. 이 차이를 인지하지 못하면 런타임 에러가 발생한다.
- **JPA 구현체마다 집계 결과의 실제 타입이 다를 수 있다**: Hibernate는 `Long`을 반환할 수 있지만, 다른 구현체는 `BigDecimal`이나 `BigInteger`를 반환할 수도 있다. `Object[]` 캐스팅은 본질적으로 취약하다.
- **쿼리 분리는 유효한 해결책이다**: 쿼리 2개가 1개보다 약간의 오버헤드가 있지만, 타입 안전성과 코드 가독성 면에서 훨씬 낫다. 성능이 정말 중요한 경우에는 DTO 프로젝션이나 Tuple을 활용하는 방법도 있다.
- **`COALESCE`를 사용해도 `null` 문제를 완전히 피할 수 없다**: 반환 타입이 `Object[]`일 때는 `COALESCE`가 있어도 캐스팅 로직이 필요하므로, 단일 값 반환으로 만드는 것이 가장 깔끔하다.
