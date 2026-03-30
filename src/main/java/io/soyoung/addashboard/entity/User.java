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
@Table(name = "users", indexes = {
    @Index(name = "idx_utm_campaign", columnList = "utm_campaign")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String email;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(length = 50)
    private String utmSource;

    @Column(length = 50)
    private String utmMedium;

    @Column(length = 255)
    private String utmCampaign;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SignupPath signupPath;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @Builder
    public User(String email, String name, String utmSource, String utmMedium,
        String utmCampaign, SignupPath signupPath) {
        this.email = email;
        this.name = name;
        this.utmSource = utmSource;
        this.utmMedium = utmMedium;
        this.utmCampaign = utmCampaign;
        this.signupPath = signupPath;
    }
}
