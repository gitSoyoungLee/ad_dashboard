package io.soyoung.addashboard.repository;

import io.soyoung.addashboard.entity.Conversion;
import java.time.LocalDateTime;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConversionRepository extends JpaRepository<Conversion, Long> {

    long countByCampaignId(String campaignId);

    long countByCampaignIdAndCreatedAtBetween(String campaignId, LocalDateTime start,
        LocalDateTime end);
}
