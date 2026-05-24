package com.codenbugs.adsservice.services.ads.creators;

import com.codenbugs.adsservice.dtos.ads.AdRequest;
import com.codenbugs.adsservice.enums.AdType;
import com.codenbugs.adsservice.exceptions.BusinessRuleException;
import com.codenbugs.adsservice.models.AdEntity;
import com.codenbugs.adsservice.models.AdPriceEntity;
import com.codenbugs.adsservice.models.AdvertiserEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.UUID;

@Component
public class TextAdCreator implements AdCreator{
    @Override
    public AdType supportedType() {
        return AdType.TEXT;
    }

    @Override
    public AdEntity createAd(AdRequest adRequest, AdPriceEntity adPrice, AdvertiserEntity advertiser,
                             MultipartFile image, MultipartFile videoFile) throws BusinessRuleException {
        if (adRequest.getContentText() == null || adRequest.getContentText().isBlank()) {
            throw new BusinessRuleException("El contenido de texto es obligatorio");
        }

        AdEntity ad = new AdEntity();
        ad.setAdvertiser(advertiser);
        ad.setAdType(adPrice.getAdType());
        ad.setAdPeriod(adPrice.getAdPeriod());
        ad.setContentText(adRequest.getContentText());
        ad.setAmountPaid(adPrice.getPrice());
        ad.setStartDate(LocalDateTime.now());
        ad.setEndDate(LocalDateTime.now().plusDays(adPrice.getAdPeriod().getDays()));
        ad.setActive(false); // Se activa después de la aprobacion
        return ad;
    }
}
