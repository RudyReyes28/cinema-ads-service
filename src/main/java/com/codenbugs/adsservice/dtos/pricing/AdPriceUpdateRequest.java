package com.codenbugs.adsservice.dtos.pricing;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Value;

import java.math.BigDecimal;
import java.util.UUID;

@Value
public class AdPriceUpdateRequest {
    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
    BigDecimal price;

}
