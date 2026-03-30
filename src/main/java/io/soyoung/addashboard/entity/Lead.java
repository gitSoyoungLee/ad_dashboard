package io.soyoung.addashboard.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "leads", indexes = {
    @Index(name = "idx_meta_campaign", columnList = "meta_campaign_id")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Lead {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 50)
    private String name;

    @Column(nullable = false, length = 20)
    private String phoneNumber;

    @Column(nullable = false, length = 50)
    private String metaCampaignId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "converted_user_id")
    private User convertedUser;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LeadStatus status;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @Builder
    public Lead(String name, String phoneNumber, String metaCampaignId,
        User convertedUser, LeadStatus status) {
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.metaCampaignId = metaCampaignId;
        this.convertedUser = convertedUser;
        this.status = status;
    }
}
