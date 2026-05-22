package com.codenbugs.adsservice.services.ads;

import com.codenbugs.adsservice.dtos.ads.AdRequest;
import com.codenbugs.adsservice.dtos.ads.AdResponse;
import com.codenbugs.adsservice.dtos.ads.AdvertiserAds;
import com.codenbugs.adsservice.exceptions.BusinessRuleException;
import com.codenbugs.adsservice.exceptions.FileStorageException;
import com.codenbugs.adsservice.exceptions.NotFoundException;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

public interface AdService {

    public AdResponse createAd(AdRequest request, MultipartFile image,
                               MultipartFile videoFile, UUID advertiserId,
                               String token) throws NotFoundException, BusinessRuleException, FileStorageException;

    public List<AdResponse> getAdsByAdvertiser(UUID advertiserId) throws NotFoundException;

    public AdResponse desactivateAd(UUID adId, UUID advertiserId) throws NotFoundException, BusinessRuleException;

    public List<AdResponse> getActiveAds() throws NotFoundException;

    public List<AdvertiserAds> getAllAdvertiser() throws NotFoundException;

}
