package com.codenbugs.adsservice.models;

import com.codenbugs.adsservice.enums.AdPeriod;
import com.codenbugs.adsservice.enums.AdType;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity(name ="Ad")
@Table(name = "ads")
@Data
@NoArgsConstructor
public class AdEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "advertiser_id", nullable = false)
    private AdvertiserEntity advertiser;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", columnDefinition = "ad_type", nullable = false)
    private AdType adType;

    @Enumerated(EnumType.STRING)
    @Column(name = "period", columnDefinition = "ad_period", nullable = false)
    private AdPeriod adPeriod;

    @Column(name = "content_text", nullable = false)
    private String contentText;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "video_url", nullable = false)
    private String videoUrl;

    @Column(name = "amount_paid", nullable = false)
    private BigDecimal amountPaid;

    @Column(name = "start_date", nullable = false)
    private LocalDateTime startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDateTime endDate;

    @Column(name = "active", nullable = false)
    private boolean active;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    @Column(name = "deactivated_by")
    private UUID deactivatedBy;

    @Column(name = "deactivated_at")
    private LocalDateTime deactivatedAt;
}
