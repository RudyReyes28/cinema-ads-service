package com.codenbugs.adsservice.dtos.pricing;

import com.codenbugs.adsservice.models.AdPriceEntity;
import lombok.Value;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Value
public class AdPriceResponse {
    UUID id;
    String adType;
    String adPeriod;
    BigDecimal price;
    LocalDateTime createdAt;
    UUID updatedBy;

    public static AdPriceResponse fromEntity(AdPriceEntity adPriceEntity) {
        return new AdPriceResponse(
                adPriceEntity.getId(),
                adPriceEntity.getAdType().name(),
                adPriceEntity.getAdPeriod().name(),
                adPriceEntity.getPrice(),
                adPriceEntity.getCreatedAt(),
                adPriceEntity.getUpdatedBy()
        );
    }
}
