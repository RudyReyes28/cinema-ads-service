package com.codenbugs.adsservice.events;

import com.codenbugs.adsservice.models.AdEntity;
import lombok.Value;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Value
public class AdCreatedEvent {
    UUID adId;
    UUID advertiserId;
    String advertiserName;
    String adType;
    String adPeriod;
    BigDecimal amountPaid;
    LocalDateTime startDate;
    LocalDateTime endDate;
    LocalDateTime createdAt;

    public static AdCreatedEvent fromEntity(AdEntity ad) {
        return new AdCreatedEvent(
                ad.getId(),
                ad.getAdvertiser().getId(),
                ad.getAdvertiser().getName(),
                ad.getAdType().name(),
                ad.getAdPeriod().name(),
                ad.getAmountPaid(),
                ad.getStartDate(),
                ad.getEndDate(),
                ad.getCreatedAt()
        );
    }
}
