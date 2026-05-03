package com.codenbugs.adsservice.services.ads.creators;

import com.codenbugs.adsservice.dtos.ads.AdRequest;
import com.codenbugs.adsservice.enums.AdType;
import com.codenbugs.adsservice.exceptions.BusinessRuleException;
import com.codenbugs.adsservice.exceptions.FileStorageException;
import com.codenbugs.adsservice.models.AdEntity;
import com.codenbugs.adsservice.models.AdPriceEntity;
import com.codenbugs.adsservice.models.AdvertiserEntity;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

public interface AdCreator {
    AdType supportedType();

    AdEntity createAd(AdRequest adRequest, AdPriceEntity adPrice, AdvertiserEntity advertiser, MultipartFile image, MultipartFile videoFile) throws BusinessRuleException, FileStorageException;
}
