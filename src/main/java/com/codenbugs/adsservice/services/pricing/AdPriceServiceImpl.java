package com.codenbugs.adsservice.services.pricing;

import com.codenbugs.adsservice.dtos.pricing.AdPriceRequest;
import com.codenbugs.adsservice.dtos.pricing.AdPriceResponse;
import com.codenbugs.adsservice.dtos.pricing.AdPriceUpdateRequest;
import com.codenbugs.adsservice.exceptions.BusinessRuleException;
import com.codenbugs.adsservice.exceptions.NotFoundException;
import com.codenbugs.adsservice.models.AdPriceEntity;
import com.codenbugs.adsservice.repositories.AdPriceRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class AdPriceServiceImpl implements AdPriceService{

    private final AdPriceRepository adPriceRepository;

    public AdPriceServiceImpl(AdPriceRepository adPriceRepository) {
        this.adPriceRepository = adPriceRepository;
    }

    @Override
    public List<AdPriceResponse> getAllAdPrices() throws BusinessRuleException {
        return adPriceRepository.findAll().stream()
                .map(AdPriceResponse::fromEntity)
                .toList();
    }

    @Override
    public AdPriceResponse createAdPrice(AdPriceRequest request, UUID adminId) throws BusinessRuleException {
        //Verificamos si ya existe un precio para el tipo de anuncio y periodo especificados
        if(adPriceRepository.existsByAdTypeAndAdPeriod(request.getAdType(), request.getAdPeriod())) {
            throw new BusinessRuleException("Ya existe un precio para el tipo de anuncio y periodo especificados");
        }

        AdPriceEntity adPriceEntity = request.toEntity(adminId);
        AdPriceEntity savedEntity = adPriceRepository.save(adPriceEntity);
        return AdPriceResponse.fromEntity(savedEntity);
    }

    @Override
    public AdPriceResponse updateAdPrice(UUID idPrice, AdPriceUpdateRequest request, UUID adminId) throws BusinessRuleException, NotFoundException {
        //Verificamos si el precio existe
        AdPriceEntity adPriceEntity = adPriceRepository.findById(idPrice)
                .orElseThrow(()-> new NotFoundException("Precio de la publicidad no encontrada"));

        //Actualizamos los campos del precio
        adPriceEntity.setPrice(request.getPrice());
        adPriceEntity.setUpdatedBy(adminId);

        AdPriceEntity updatedEntity = adPriceRepository.save(adPriceEntity);
        return AdPriceResponse.fromEntity(updatedEntity);

    }

    @Override
    public void deleteAdPrice(UUID idPrice) throws BusinessRuleException, NotFoundException {
        //Verificamos si el precio existe
        if(!adPriceRepository.existsById(idPrice)) {
            throw new NotFoundException("Precio de la publicidad no encontrada");
        }

        adPriceRepository.deleteById(idPrice);

    }
}
