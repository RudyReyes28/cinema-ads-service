package com.codenbugs.adsservice.dtos.pricing;

import com.codenbugs.adsservice.enums.AdPeriod;
import com.codenbugs.adsservice.enums.AdType;
import com.codenbugs.adsservice.models.AdPriceEntity;
import jakarta.validation.constraints.*;
import lombok.Value;

import java.math.BigDecimal;
import java.util.UUID;

@Value
public class AdPriceRequest {
    @NotNull(message = "Ad type is required")
    AdType adType;
    @NotNull(message = "Ad period is required")
    AdPeriod adPeriod;
    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
    BigDecimal price;


    public AdPriceEntity toEntity(UUID createdBy) {
        AdPriceEntity adPriceEntity = new AdPriceEntity();
        adPriceEntity.setAdType(adType);
        adPriceEntity.setAdPeriod(adPeriod);
        adPriceEntity.setPrice(price);
        adPriceEntity.setUpdatedBy(createdBy);
        return adPriceEntity;
    }
}
