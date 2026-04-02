package io.soyoung.addashboard.repository;

import io.soyoung.addashboard.entity.Lead;
import io.soyoung.addashboard.entity.LeadStatus;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LeadRepository extends JpaRepository<Lead, Long> {

    Optional<Lead> findByEmail(String email);

    List<Lead> findAllByEmail(String email);

    long countByMetaCampaignId(String metaCampaignId);

    long countByStatus(LeadStatus status);

    List<Lead> findAllByStatus(LeadStatus status);

    List<Lead> findAllByMetaCampaignId(String metaCampaignId);

    long countByStatusAndCreatedAtBetween(LeadStatus status, LocalDateTime start,
        LocalDateTime end);

    @Query("SELECT CAST(l.createdAt AS LocalDate), COUNT(l) FROM Lead l "
        + "WHERE l.status = :status AND l.createdAt BETWEEN :start AND :end "
        + "GROUP BY CAST(l.createdAt AS LocalDate) "
        + "ORDER BY CAST(l.createdAt AS LocalDate)")
    List<Object[]> countDailyLeadsByStatusBetween(@Param("status") LeadStatus status,
        @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
}
