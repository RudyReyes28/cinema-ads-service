package com.codenbugs.adsservice.services.ads;

import com.codenbugs.adsservice.clients.wallet.WalletClient;
import com.codenbugs.adsservice.dtos.ads.AdRequest;
import com.codenbugs.adsservice.dtos.ads.AdResponse;
import com.codenbugs.adsservice.dtos.ads.AdvertiserAds;
import com.codenbugs.adsservice.events.AdCreatedEvent;
import com.codenbugs.adsservice.exceptions.BusinessRuleException;
import com.codenbugs.adsservice.exceptions.FileStorageException;
import com.codenbugs.adsservice.exceptions.NotFoundException;
import com.codenbugs.adsservice.kafka.AdKafkaProducer;
import com.codenbugs.adsservice.models.AdEntity;
import com.codenbugs.adsservice.models.AdPriceEntity;
import com.codenbugs.adsservice.models.AdvertiserEntity;
import com.codenbugs.adsservice.repositories.AdPriceRepository;
import com.codenbugs.adsservice.repositories.AdRepository;
import com.codenbugs.adsservice.repositories.AdvertiserRepository;
import com.codenbugs.adsservice.services.ads.creators.AdCreator;
import com.codenbugs.adsservice.services.ads.creators.AdCreatorFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class AdServiceImpl implements AdService {
    private final AdRepository adRepository;
    private final AdPriceRepository adPriceRepository;
    private final AdCreatorFactory adCreatorFactory;
    private final WalletClient walletClient;
    private final AdvertiserRepository advertiserRepository;
    private final AdKafkaProducer adKafkaProducer;

    public AdServiceImpl(AdRepository adRepository, AdPriceRepository adPriceRepository, AdCreatorFactory adCreatorFactory, WalletClient walletClient, AdvertiserRepository advertiserRepository, AdKafkaProducer adKafkaProducer) {
        this.adRepository = adRepository;
        this.adPriceRepository = adPriceRepository;
        this.adCreatorFactory = adCreatorFactory;
        this.walletClient = walletClient;
        this.advertiserRepository = advertiserRepository;
        this.adKafkaProducer = adKafkaProducer;
    }

    @Override
    public AdResponse createAd(AdRequest request, MultipartFile image, MultipartFile videoFile, UUID advertiserId, String token) throws NotFoundException, BusinessRuleException, FileStorageException {
        //Obtenemos el precio del anuncio
        AdPriceEntity adPrice = adPriceRepository.findById(request.getAdPriceId())
                .orElseThrow(() -> new NotFoundException("Precio de anuncio no encontrado"));

        //Obtenemos el anunciante
        AdvertiserEntity advertiser = advertiserRepository.findById(advertiserId)
                .orElseThrow(() -> new NotFoundException("Anunciante no encontrado"));

        // Seleccionamos el creador adecuado según el tipo de anuncio
        AdCreator adCreator = adCreatorFactory.getCreator(adPrice.getAdType());

        //Construimos el anuncio
        AdEntity adEntity = adCreator.createAd(request, adPrice,advertiser , image, videoFile);

        // Guardamos el anuncio
        AdEntity savedAd = adRepository.save(adEntity);

        // Realizamos el cargo en la billetera del anunciante
        try {
            walletClient.deduct(advertiserId, adPrice.getPrice(), token);
        } catch (Exception e) {
            // Si el cargo falla, eliminamos el anuncio creado para mantener la consistencia
            adRepository.delete(savedAd);
            throw new BusinessRuleException("No se pudo procesar el pago");
        }

        //Cambiamos el estado del anuncio a activo
        savedAd.setActive(true);
        adRepository.save(savedAd);

        //Aqui mandariamos el mensaje a kafka para que el servicio de reportes lo consuma y genere el reporte del anuncio
        AdCreatedEvent event = AdCreatedEvent.fromEntity(savedAd);
        adKafkaProducer.publishAdCreatedEvent(event);

        // Devolvemos la respuesta
        return AdResponse.fromEntity(savedAd);
    }

    @Override
    public List<AdResponse> getAdsByAdvertiser(UUID advertiserId) throws NotFoundException {
        // Verificamos que el anunciante exista
        if (!advertiserRepository.existsById(advertiserId)) {
            throw new NotFoundException("Anunciante no encontrado");
        }
        return adRepository.findByAdvertiserId(advertiserId).stream()
                .map(AdResponse::fromEntity)
                .toList();
    }

    @Override
    public AdResponse desactivateAd(UUID adId, UUID advertiserId) throws NotFoundException, BusinessRuleException {
        // Buscamos el anuncio
        AdEntity adEntity = adRepository.findById(adId)
                .orElseThrow(() -> new NotFoundException("Anuncio no encontrado"));

        // Desactivamos el anuncio
        adEntity.setActive(false);
        adEntity.setDeactivatedBy(advertiserId);
        adEntity.setDeactivatedAt(LocalDateTime.now());
        adEntity.setUpdatedAt(LocalDateTime.now());
        adRepository.save(adEntity);

        return AdResponse.fromEntity(adEntity);
    }

    @Override
    public List<AdResponse> getActiveAds() throws NotFoundException {
        return adRepository.findByActiveTrueAndStartDateLessThanEqualAndEndDateGreaterThanEqual(LocalDateTime.now(), LocalDateTime.now())
                .stream()
                .map(AdResponse::fromEntity)
                .toList();
    }

    @Override
    public List<AdvertiserAds> getAllAdvertiser() throws NotFoundException {
        //Obtenemos todos los anunciantes
        List<AdvertiserEntity> advertisers = advertiserRepository.findAll();

        //Contamos cuantos anuncios tiene cada anunciante
        return advertisers.stream()
                .map(advertiser -> {
                    int adCount = adRepository.countByAdvertiserId(advertiser.getId());
                    return new AdvertiserAds(advertiser.getId(), advertiser.getName(), adCount);
                })
                .toList();

    }
}
