package com.codenbugs.adsservice.services.ads;

import com.codenbugs.adsservice.clients.wallet.WalletClient;
import com.codenbugs.adsservice.dtos.ads.AdRequest;
import com.codenbugs.adsservice.dtos.ads.AdResponse;
import com.codenbugs.adsservice.enums.AdPeriod;
import com.codenbugs.adsservice.enums.AdType;
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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AdServiceImplTest {

    // -------------------- Constants --------------------

    private static final UUID        ID_AD          = UUID.randomUUID();
    private static final UUID        ID_ADVERTISER  = UUID.randomUUID();
    private static final UUID        ID_PRICE       = UUID.randomUUID();
    private static final String      TOKEN          = "Bearer test-token";
    private static final String      CONTENT_TEXT   = "Texto del anuncio";
    private static final BigDecimal  PRICE          = new BigDecimal("10.00");
    private static final AdType      AD_TYPE        = AdType.TEXT;
    private static final AdPeriod    AD_PERIOD      = AdPeriod.ONE_DAY;

    // --------------------- Mocks --------------------------

    @Mock private AdRepository        adRepository;
    @Mock private AdPriceRepository   adPriceRepository;
    @Mock private AdCreatorFactory    adCreatorFactory;
    @Mock private WalletClient        walletClient;
    @Mock private AdvertiserRepository advertiserRepository;
    @Mock private AdCreator           adCreator;
    @Mock private MultipartFile       image;
    @Mock private MultipartFile       videoFile;
    @Mock private AdKafkaProducer adKafkaProducer;

    @InjectMocks
    private AdServiceImpl adService;

    // ------------------ Helpers ---------------------------

    private AdPriceEntity adPrice() {
        AdPriceEntity price = new AdPriceEntity();
        price.setId(ID_PRICE);
        price.setAdType(AD_TYPE);
        price.setAdPeriod(AD_PERIOD);
        price.setPrice(PRICE);
        return price;
    }

    private AdvertiserEntity advertiser() {
        AdvertiserEntity advertiser = new AdvertiserEntity();
        advertiser.setId(ID_ADVERTISER);
        return advertiser;
    }

    private AdEntity adEntity(boolean active) {
        AdEntity ad = new AdEntity();
        ad.setId(ID_AD);
        ad.setAdvertiser(advertiser());
        ad.setAdType(AD_TYPE);
        ad.setAdPeriod(AD_PERIOD);
        ad.setContentText(CONTENT_TEXT);
        ad.setAmountPaid(PRICE);
        ad.setStartDate(LocalDateTime.now());
        ad.setEndDate(LocalDateTime.now().plusDays(1));
        ad.setActive(active);
        ad.setCreatedAt(LocalDateTime.now());
        ad.setUpdatedAt(LocalDateTime.now());
        return ad;
    }

    private AdRequest adRequest() {
        AdRequest request = new AdRequest(ID_PRICE, CONTENT_TEXT, null);
        return request;
    }

    // ---------------------------
    // createAd
    // ---------------------------

    @Test
    void createAd_WhenValidRequest_CreatesAndActivatesAd() throws Exception {
        // Arrange
        ArgumentCaptor<AdEntity> captor = ArgumentCaptor.forClass(AdEntity.class);
        AdEntity pending = adEntity(false);
        AdEntity active  = adEntity(true);

        when(adPriceRepository.findById(ID_PRICE)).thenReturn(Optional.of(adPrice()));
        when(advertiserRepository.findById(ID_ADVERTISER)).thenReturn(Optional.of(advertiser()));
        when(adCreatorFactory.getCreator(AD_TYPE)).thenReturn(adCreator);
        when(adCreator.createAd(any(), any(), any(), any(), any())).thenReturn(pending);
        when(adRepository.save(any())).thenReturn(pending).thenReturn(active);

        // Act
        AdResponse result = adService.createAd(adRequest(), image, videoFile, ID_ADVERTISER, TOKEN);

        // Assert
        assertAll(
                () -> verify(adRepository, times(2)).save(captor.capture()),
                () -> assertTrue(captor.getAllValues().get(0).isActive()),  // primer save: PENDING
                () -> assertTrue(captor.getAllValues().get(1).isActive()),   // segundo save: ACTIVE
                () -> verify(walletClient).deduct(ID_ADVERTISER, PRICE, TOKEN),
                () -> verify(adKafkaProducer).publishAdCreatedEvent(any()),
                () -> assertNotNull(result),
                () -> assertEquals(ID_AD, result.getIdAd())
        );
    }

    @Test
    void createAd_WhenPriceNotFound_ThrowsNotFoundException() {
        // Arrange
        when(adPriceRepository.findById(ID_PRICE)).thenReturn(Optional.empty());

        // Act & Assert
        assertAll(
                () -> assertThrows(NotFoundException.class,
                        () -> adService.createAd(adRequest(), image, videoFile, ID_ADVERTISER, TOKEN)
                ),
                () -> verify(adRepository, never()).save(any())
        );
    }

    @Test
    void createAd_WhenAdvertiserNotFound_ThrowsNotFoundException() {
        // Arrange
        when(adPriceRepository.findById(ID_PRICE)).thenReturn(Optional.of(adPrice()));
        when(advertiserRepository.findById(ID_ADVERTISER)).thenReturn(Optional.empty());

        // Act & Assert
        assertAll(
                () -> assertThrows(NotFoundException.class,
                        () -> adService.createAd(adRequest(), image, videoFile, ID_ADVERTISER, TOKEN)
                ),
                () -> verify(adRepository, never()).save(any())
        );
    }

    @Test
    void createAd_WhenWalletDeductFails_DeletesAdAndThrowsBusinessRuleException() throws Exception {
        // Arrange
        AdEntity pending = adEntity(false);

        when(adPriceRepository.findById(ID_PRICE)).thenReturn(Optional.of(adPrice()));
        when(advertiserRepository.findById(ID_ADVERTISER)).thenReturn(Optional.of(advertiser()));
        when(adCreatorFactory.getCreator(AD_TYPE)).thenReturn(adCreator);
        when(adCreator.createAd(any(), any(), any(), any(), any())).thenReturn(pending);
        when(adRepository.save(any())).thenReturn(pending);
        doThrow(new BusinessRuleException("Saldo insuficiente"))
                .when(walletClient).deduct(any(), any(), any());

        // Act & Assert
        assertAll(
                () -> assertThrows(BusinessRuleException.class,
                        () -> adService.createAd(adRequest(), image, videoFile, ID_ADVERTISER, TOKEN)
                ),
                () -> verify(adRepository).delete(pending)  // compensación
        );
    }

    @Test
    void createAd_WhenCreatorThrowsBusinessRuleException_DoesNotSaveAd() throws Exception {
        // Arrange
        when(adPriceRepository.findById(ID_PRICE)).thenReturn(Optional.of(adPrice()));
        when(advertiserRepository.findById(ID_ADVERTISER)).thenReturn(Optional.of(advertiser()));
        when(adCreatorFactory.getCreator(AD_TYPE)).thenReturn(adCreator);
        when(adCreator.createAd(any(), any(), any(), any(), any()))
                .thenThrow(new BusinessRuleException("Contenido inválido"));

        // Act & Assert
        assertAll(
                () -> assertThrows(BusinessRuleException.class,
                        () -> adService.createAd(adRequest(), image, videoFile, ID_ADVERTISER, TOKEN)
                ),
                () -> verify(adRepository, never()).save(any()),
                () -> verify(walletClient, never()).deduct(any(), any(), any())
        );
    }

    // ---------------------------
    // getAdsByAdvertiser
    // ---------------------------

    @Test
    void getAdsByAdvertiser_WhenAdvertiserExists_ReturnsAdList() throws NotFoundException {
        // Arrange
        when(advertiserRepository.existsById(ID_ADVERTISER)).thenReturn(true);
        when(adRepository.findByAdvertiserId(ID_ADVERTISER)).thenReturn(List.of(adEntity(true)));

        // Act
        List<AdResponse> result = adService.getAdsByAdvertiser(ID_ADVERTISER);

        // Assert
        assertAll(
                () -> assertNotNull(result),
                () -> assertEquals(1, result.size()),
                () -> assertEquals(ID_AD, result.get(0).getIdAd())
        );
    }

    @Test
    void getAdsByAdvertiser_WhenNoAds_ReturnsEmptyList() throws NotFoundException {
        // Arrange
        when(advertiserRepository.existsById(ID_ADVERTISER)).thenReturn(true);
        when(adRepository.findByAdvertiserId(ID_ADVERTISER)).thenReturn(List.of());

        // Act
        List<AdResponse> result = adService.getAdsByAdvertiser(ID_ADVERTISER);

        // Assert
        assertAll(
                () -> assertNotNull(result),
                () -> assertTrue(result.isEmpty())
        );
    }

    @Test
    void getAdsByAdvertiser_WhenAdvertiserNotFound_ThrowsNotFoundException() {
        // Arrange
        when(advertiserRepository.existsById(ID_ADVERTISER)).thenReturn(false);

        // Act & Assert
        assertThrows(NotFoundException.class,
                () -> adService.getAdsByAdvertiser(ID_ADVERTISER)
        );
    }

    // ---------------------------
    // desactivateAd
    // ---------------------------

    @Test
    void desactivateAd_WhenAdExists_DeactivatesCorrectly() throws Exception {
        // Arrange
        ArgumentCaptor<AdEntity> captor = ArgumentCaptor.forClass(AdEntity.class);
        AdEntity active = adEntity(true);

        when(adRepository.findById(ID_AD)).thenReturn(Optional.of(active));
        when(adRepository.save(any())).thenReturn(active);

        // Act
        AdResponse result = adService.desactivateAd(ID_AD, ID_ADVERTISER);

        // Assert
        assertAll(
                () -> verify(adRepository).save(captor.capture()),
                () -> assertFalse(captor.getValue().isActive()),
                () -> assertEquals(ID_ADVERTISER, captor.getValue().getDeactivatedBy()),
                () -> assertNotNull(captor.getValue().getDeactivatedAt()),
                () -> assertNotNull(result)
        );
    }

    @Test
    void desactivateAd_WhenAdNotFound_ThrowsNotFoundException() {
        // Arrange
        when(adRepository.findById(ID_AD)).thenReturn(Optional.empty());

        // Act & Assert
        assertAll(
                () -> assertThrows(NotFoundException.class,
                        () -> adService.desactivateAd(ID_AD, ID_ADVERTISER)
                ),
                () -> verify(adRepository, never()).save(any())
        );
    }

    // ---------------------------
    // getActiveAds
    // ---------------------------

    @Test
    void getActiveAds_WhenActiveAdsExist_ReturnsActiveAdList() throws NotFoundException {
        // Arrange
        when(adRepository.findByActiveTrueAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(List.of(adEntity(true)));

        // Act
        List<AdResponse> result = adService.getActiveAds();

        // Assert
        assertAll(
                () -> assertNotNull(result),
                () -> assertEquals(1, result.size()),
                () -> assertTrue(result.get(0).getActive())
        );
    }

    @Test
    void getActiveAds_WhenNoActiveAds_ReturnsEmptyList() throws NotFoundException {
        // Arrange
        when(adRepository.findByActiveTrueAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(List.of());

        // Act
        List<AdResponse> result = adService.getActiveAds();

        // Assert
        assertAll(
                () -> assertNotNull(result),
                () -> assertTrue(result.isEmpty())
        );
    }
}