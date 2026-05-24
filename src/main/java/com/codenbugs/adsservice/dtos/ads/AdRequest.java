package com.codenbugs.adsservice.dtos.ads;

import com.codenbugs.adsservice.enums.AdPeriod;
import com.codenbugs.adsservice.enums.AdType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Value;

import java.util.UUID;

@Value
public class AdRequest {

    @NotNull(message = "Ad price is required")
    UUID adPriceId;

    @NotBlank(message = "Content text is required")
    String contentText;

    // Solo para VIDEO_TEXT cuando manda URL en lugar de archivo
    String videoUrl;
}
