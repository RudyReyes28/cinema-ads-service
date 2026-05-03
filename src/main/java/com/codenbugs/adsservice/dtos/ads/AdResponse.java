package com.codenbugs.adsservice.dtos.ads;

import com.codenbugs.adsservice.enums.AdPeriod;
import com.codenbugs.adsservice.enums.AdType;
import com.codenbugs.adsservice.models.AdEntity;
import lombok.Value;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Value
public class AdResponse {
    UUID idAd;
    UUID idAdvertiser;
    AdType adType;
    AdPeriod adPeriod;
    String contentText;
    String imageUrl;
    String videoUrl;
    BigDecimal amountPaid;
    LocalDateTime startDate;
    LocalDateTime endDate;
    Boolean active;
    LocalDateTime createdAt;
    UUID desactivateBy;
    LocalDateTime updatedAt;

    public static AdResponse fromEntity(AdEntity ad) {
        return new AdResponse(
                ad.getId(),
                ad.getAdvertiser().getId(),
                ad.getAdType(),
                ad.getAdPeriod(),
                ad.getContentText(),
                ad.getImageUrl(),
                ad.getVideoUrl(),
                ad.getAmountPaid(),
                ad.getStartDate(),
                ad.getEndDate(),
                ad.isActive(),
                ad.getCreatedAt(),
                ad.getDeactivatedBy(),
                ad.getUpdatedAt()
        );
    }

}
