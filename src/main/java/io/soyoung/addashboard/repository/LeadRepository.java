package io.soyoung.addashboard.repository;

import io.soyoung.addashboard.entity.Lead;
import io.soyoung.addashboard.entity.LeadStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LeadRepository extends JpaRepository<Lead, Long> {

    Optional<Lead> findByEmail(String email);

    List<Lead> findAllByEmail(String email);

    long countByMetaCampaignId(String metaCampaignId);

    long countByStatus(LeadStatus status);
}
