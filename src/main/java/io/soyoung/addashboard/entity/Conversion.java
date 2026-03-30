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
@Table(name = "conversions", indexes = {
    @Index(name = "idx_campaign_date", columnList = "campaign_id, created_at")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Conversion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ConversionType type;

    @Column(nullable = false, length = 50)
    private String campaignId;

    @Column(nullable = false)
    private Integer originalDataId;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @Builder
    public Conversion(ConversionType type, String campaignId, Integer originalDataId) {
        this.type = type;
        this.campaignId = campaignId;
        this.originalDataId = originalDataId;
    }
}
