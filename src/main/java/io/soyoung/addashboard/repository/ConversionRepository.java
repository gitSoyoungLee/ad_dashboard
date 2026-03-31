package io.soyoung.addashboard.repository;

import io.soyoung.addashboard.entity.Conversion;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConversionRepository extends JpaRepository<Conversion, Long> {

    long countByCampaignId(String campaignId);

    long countByCampaignIdAndCreatedAtBetween(String campaignId, LocalDateTime start,
        LocalDateTime end);

    long countByCampaignIdInAndCreatedAtBetween(List<String> campaignIds, LocalDateTime start,
        LocalDateTime end);
}
