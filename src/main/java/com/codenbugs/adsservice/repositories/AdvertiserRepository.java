package com.codenbugs.adsservice.repositories;

import com.codenbugs.adsservice.models.AdvertiserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface AdvertiserRepository extends JpaRepository<AdvertiserEntity, UUID> {
}
