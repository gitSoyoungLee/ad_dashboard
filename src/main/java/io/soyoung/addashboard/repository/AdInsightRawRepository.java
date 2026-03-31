package io.soyoung.addashboard.repository;

import io.soyoung.addashboard.entity.AdInsightRaw;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AdInsightRawRepository extends JpaRepository<AdInsightRaw, Long> {

    List<AdInsightRaw> findAllByMetaIdAndLogDateBetween(String metaId, LocalDate start,
        LocalDate end);

    @Query("SELECT COALESCE(SUM(a.spend), 0) FROM AdInsightRaw a "
        + "WHERE a.logDate BETWEEN :start AND :end")
    BigDecimal sumSpendBetween(@Param("start") LocalDate start, @Param("end") LocalDate end);

    @Query("SELECT COALESCE(SUM(a.spend), 0) FROM AdInsightRaw a "
        + "WHERE a.metaId IN :metaIds AND a.logDate BETWEEN :start AND :end")
    BigDecimal sumSpendByMetaIdsAndDateBetween(@Param("metaIds") List<String> metaIds,
        @Param("start") LocalDate start, @Param("end") LocalDate end);

    @Query("SELECT COALESCE(SUM(a.spend), 0) FROM AdInsightRaw a, AdEntity e "
        + "WHERE a.metaId = e.metaId "
        + "AND e.adCategory = :category "
        + "AND a.logDate BETWEEN :start AND :end")
    BigDecimal sumSpendByCategoryAndDateBetween(
        @Param("category") io.soyoung.addashboard.entity.AdCategory category,
        @Param("start") LocalDate start, @Param("end") LocalDate end);

    @Query("SELECT a.logDate, COALESCE(SUM(a.spend), 0), "
        + "COALESCE(SUM(a.impressions), 0), COALESCE(SUM(a.clicks), 0) "
        + "FROM AdInsightRaw a "
        + "WHERE a.logDate BETWEEN :start AND :end "
        + "GROUP BY a.logDate ORDER BY a.logDate")
    List<Object[]> findDailyStatsBetween(@Param("start") LocalDate start,
        @Param("end") LocalDate end);
}
