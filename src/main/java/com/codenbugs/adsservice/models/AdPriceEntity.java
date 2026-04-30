package com.codenbugs.adsservice.models;

import com.codenbugs.adsservice.enums.AdPeriod;
import com.codenbugs.adsservice.enums.AdType;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity(name ="AdPrice")
@Table(name = "ad_prices")
@Data
@NoArgsConstructor
public class AdPriceEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(name = "ad_type", columnDefinition = "ad_type", nullable = false)
    private AdType adType;

    @Enumerated(EnumType.STRING)
    @Column(name = "period", columnDefinition = "ad_period", nullable = false)
    private AdPeriod adPeriod;

    @Column(name = "price", nullable = false)
    private BigDecimal price;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_by", nullable = false)
    private UUID updatedBy;

}
