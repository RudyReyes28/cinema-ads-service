package com.codenbugs.adsservice.repositories;

import com.codenbugs.adsservice.models.AdEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Repository
public interface AdRepository extends JpaRepository<AdEntity, UUID> {
    List<AdEntity> findByAdvertiserId(UUID advertiserId);

    List<AdEntity> findByActiveTrueAndStartDateLessThanEqualAndEndDateGreaterThanEqual(LocalDateTime now1, LocalDateTime now2);
}
