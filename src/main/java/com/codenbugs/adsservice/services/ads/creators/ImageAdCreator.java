package com.codenbugs.adsservice.services.ads.creators;

import com.codenbugs.adsservice.dtos.ads.AdRequest;
import com.codenbugs.adsservice.enums.AdType;
import com.codenbugs.adsservice.exceptions.BusinessRuleException;
import com.codenbugs.adsservice.exceptions.FileStorageException;
import com.codenbugs.adsservice.models.AdEntity;
import com.codenbugs.adsservice.models.AdPriceEntity;
import com.codenbugs.adsservice.models.AdvertiserEntity;
import com.codenbugs.adsservice.services.storage.FileStorageService;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;

@Component
public class ImageAdCreator implements AdCreator {
    private static final String IMAGE_FOLDER = "ads/images";

    private final FileStorageService fileStorageService;

    public ImageAdCreator(FileStorageService fileStorageService) {
        this.fileStorageService = fileStorageService;
    }

    @Override
    public AdType supportedType() {
        return AdType.TEXT_IMAGE;
    }

    @Override
    public AdEntity createAd(AdRequest adRequest, AdPriceEntity adPrice, AdvertiserEntity advertiser,
                             MultipartFile image, MultipartFile videoFile) throws BusinessRuleException, FileStorageException {
        if (adRequest.getContentText() == null || adRequest.getContentText().isBlank()) {
            throw new BusinessRuleException("El contenido de texto es obligatorio");
        }

        if (image == null || image.isEmpty()) {
            throw new BusinessRuleException("La imagen es obligatoria para este tipo de anuncio");
        }

        String imageUrl = fileStorageService.uploadFile(image, IMAGE_FOLDER);

        AdEntity ad = new AdEntity();
        ad.setAdvertiser(advertiser);
        ad.setAdType(adPrice.getAdType());
        ad.setAdPeriod(adPrice.getAdPeriod());
        ad.setContentText(adRequest.getContentText());
        ad.setImageUrl(imageUrl);
        ad.setAmountPaid(adPrice.getPrice());
        ad.setStartDate(LocalDateTime.now());
        ad.setEndDate(LocalDateTime.now().plusDays(adPrice.getAdPeriod().getDays()));
        ad.setActive(false); // Se activa después de la aprobacion

        return ad;
    }
}
