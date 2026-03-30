package io.soyoung.addashboard.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "daily_performance_summary",
    uniqueConstraints = {
        @UniqueConstraint(name = "uq_summary", columnNames = {"log_date", "meta_id"})
    }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DailyPerformanceSummary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDate logDate;

    @Column(nullable = false, length = 50)
    private String metaId;

    @Enumerated(EnumType.STRING)
    private AdCategory adCategory;

    @Column(precision = 15, scale = 2)
    private BigDecimal totalSpend;

    private Integer totalClicks;

    private Integer internalConversionCount;

    @Column(precision = 5, scale = 2)
    private BigDecimal ctr;

    @Column(precision = 15, scale = 2)
    private BigDecimal cpc;

    @Column(precision = 15, scale = 2)
    private BigDecimal cpa;

    @Builder
    public DailyPerformanceSummary(LocalDate logDate, String metaId, AdCategory adCategory,
        BigDecimal totalSpend, Integer totalClicks, Integer internalConversionCount,
        BigDecimal ctr, BigDecimal cpc, BigDecimal cpa) {
        this.logDate = logDate;
        this.metaId = metaId;
        this.adCategory = adCategory;
        this.totalSpend = totalSpend;
        this.totalClicks = totalClicks;
        this.internalConversionCount = internalConversionCount;
        this.ctr = ctr;
        this.cpc = cpc;
        this.cpa = cpa;
    }
}
