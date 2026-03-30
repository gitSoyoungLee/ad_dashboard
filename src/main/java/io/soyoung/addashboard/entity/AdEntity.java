package io.soyoung.addashboard.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "ad_entities", indexes = {
    @Index(name = "idx_parent_meta", columnList = "parent_meta_id")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AdEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String metaId;

    @Column(length = 50)
    private String parentMetaId;

    @Column(nullable = false, length = 255)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EntityType entityType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AdCategory adCategory;

    @Column(length = 20)
    private String status;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @Builder
    public AdEntity(String metaId, String parentMetaId, String name,
        EntityType entityType, AdCategory adCategory, String status) {
        this.metaId = metaId;
        this.parentMetaId = parentMetaId;
        this.name = name;
        this.entityType = entityType;
        this.adCategory = adCategory;
        this.status = status;
    }
}
