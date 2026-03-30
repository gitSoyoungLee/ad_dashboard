package io.soyoung.addashboard.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "ad_insights_raw",
    uniqueConstraints = {
        @UniqueConstraint(name = "uq_meta_date", columnNames = {"meta_id", "log_date"})
    },
    indexes = {
        @Index(name = "idx_log_date", columnList = "log_date")
    }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AdInsightRaw {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String metaId;

    @Column(nullable = false)
    private LocalDate logDate;

    @Column(precision = 15, scale = 2)
    private BigDecimal spend;

    private Integer impressions;

    private Integer clicks;

    private Integer reach;

    @Builder
    public AdInsightRaw(String metaId, LocalDate logDate, BigDecimal spend,
        Integer impressions, Integer clicks, Integer reach) {
        this.metaId = metaId;
        this.logDate = logDate;
        this.spend = spend;
        this.impressions = impressions;
        this.clicks = clicks;
        this.reach = reach;
    }
}
