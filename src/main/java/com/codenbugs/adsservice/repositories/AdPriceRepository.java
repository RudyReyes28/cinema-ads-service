package com.codenbugs.adsservice.repositories;

import com.codenbugs.adsservice.enums.AdPeriod;
import com.codenbugs.adsservice.enums.AdType;
import com.codenbugs.adsservice.models.AdPriceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface AdPriceRepository extends JpaRepository<AdPriceEntity, UUID> {
        boolean existsByAdTypeAndAdPeriod(AdType adType, AdPeriod adPeriod);
}
