package io.soyoung.addashboard.repository;

import io.soyoung.addashboard.entity.AdCategory;
import io.soyoung.addashboard.entity.AdEntity;
import io.soyoung.addashboard.entity.EntityType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdEntityRepository extends JpaRepository<AdEntity, Long> {

    Optional<AdEntity> findByMetaId(String metaId);

    List<AdEntity> findAllByEntityType(EntityType entityType);

    List<AdEntity> findAllByEntityTypeAndAdCategory(EntityType entityType, AdCategory adCategory);

    List<AdEntity> findAllByParentMetaId(String parentMetaId);
}
