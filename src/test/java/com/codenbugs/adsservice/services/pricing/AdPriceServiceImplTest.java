package com.codenbugs.adsservice.services.pricing;

import com.codenbugs.adsservice.dtos.pricing.AdPriceRequest;
import com.codenbugs.adsservice.dtos.pricing.AdPriceResponse;
import com.codenbugs.adsservice.dtos.pricing.AdPriceUpdateRequest;
import com.codenbugs.adsservice.enums.AdPeriod;
import com.codenbugs.adsservice.enums.AdType;
import com.codenbugs.adsservice.exceptions.BusinessRuleException;
import com.codenbugs.adsservice.exceptions.NotFoundException;
import com.codenbugs.adsservice.models.AdPriceEntity;
import com.codenbugs.adsservice.repositories.AdPriceRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AdPriceServiceImplTest {

    // ---------------------- Constants ---------------------

    private static final UUID     ID_PRICE      = UUID.randomUUID();
    private static final UUID     ID_ADMIN      = UUID.randomUUID();
    private static final BigDecimal PRICE        = new BigDecimal("10.00");
    private static final BigDecimal UPDATED_PRICE = new BigDecimal("25.00");
    private static final AdType   AD_TYPE       = AdType.TEXT;
    private static final AdPeriod AD_PERIOD     = AdPeriod.ONE_DAY;

    //  ---------------------- Mocks ----------------------

    @Mock
    private AdPriceRepository adPriceRepository;

    @InjectMocks
    private AdPriceServiceImpl adPriceService;

    //  ---------------------- Helpers  ----------------------

    private AdPriceEntity adPriceEntity() {
        AdPriceEntity entity = new AdPriceEntity();
        entity.setId(ID_PRICE);
        entity.setAdType(AD_TYPE);
        entity.setAdPeriod(AD_PERIOD);
        entity.setPrice(PRICE);
        entity.setUpdatedBy(ID_ADMIN);
        entity.setCreatedAt(LocalDateTime.now());
        return entity;
    }

    private AdPriceRequest adPriceRequest() {
        return new AdPriceRequest(AD_TYPE, AD_PERIOD, PRICE);
    }

    private AdPriceUpdateRequest adPriceUpdateRequest() {
        return new AdPriceUpdateRequest(UPDATED_PRICE);
    }

    //  ---------------------- getAllAdPrices  ----------------------

    @Test
    void getAllAdPrices_WhenPricesExist_ReturnsAllPrices() throws BusinessRuleException {
        // Arrange
        when(adPriceRepository.findAll()).thenReturn(List.of(adPriceEntity()));

        // Act
        List<AdPriceResponse> result = adPriceService.getAllAdPrices();

        // Assert
        assertAll(
                () -> assertNotNull(result),
                () -> assertEquals(1, result.size()),
                () -> assertEquals(ID_PRICE,        result.get(0).getId()),
                () -> assertEquals(AD_TYPE.name(),  result.get(0).getAdType()),
                () -> assertEquals(AD_PERIOD.name(), result.get(0).getAdPeriod()),
                () -> assertEquals(PRICE,           result.get(0).getPrice())
        );
    }

    @Test
    void getAllAdPrices_WhenNoPricesExist_ReturnsEmptyList() throws BusinessRuleException {
        // Arrange
        when(adPriceRepository.findAll()).thenReturn(List.of());

        // Act
        List<AdPriceResponse> result = adPriceService.getAllAdPrices();

        // Assert
        assertAll(
                () -> assertNotNull(result),
                () -> assertTrue(result.isEmpty())
        );
    }

    //  ---------------------- createAdPrice  ----------------------

    @Test
    void createAdPrice_WhenCombinationDoesNotExist_CreatesSuccessfully() throws BusinessRuleException {
        // Arrange
        ArgumentCaptor<AdPriceEntity> captor = ArgumentCaptor.forClass(AdPriceEntity.class);

        when(adPriceRepository.existsByAdTypeAndAdPeriod(AD_TYPE, AD_PERIOD)).thenReturn(false);
        when(adPriceRepository.save(any())).thenReturn(adPriceEntity());

        // Act
        AdPriceResponse result = adPriceService.createAdPrice(adPriceRequest(), ID_ADMIN);

        // Assert
        assertAll(
                () -> verify(adPriceRepository).save(captor.capture()),
                () -> assertEquals(AD_TYPE,   captor.getValue().getAdType()),
                () -> assertEquals(AD_PERIOD, captor.getValue().getAdPeriod()),
                () -> assertEquals(PRICE,     captor.getValue().getPrice()),
                () -> assertEquals(ID_ADMIN,  captor.getValue().getUpdatedBy()),
                () -> assertEquals(ID_PRICE,  result.getId()),
                () -> assertEquals(PRICE,     result.getPrice())
        );
    }

    @Test
    void createAdPrice_WhenCombinationAlreadyExists_ThrowsBusinessRuleException() {
        // Arrange
        when(adPriceRepository.existsByAdTypeAndAdPeriod(AD_TYPE, AD_PERIOD)).thenReturn(true);

        // Act & Assert
        assertAll(
                () -> assertThrows(
                        BusinessRuleException.class,
                        () -> adPriceService.createAdPrice(adPriceRequest(), ID_ADMIN)
                ),
                () -> verify(adPriceRepository, never()).save(any())
        );
    }

    //  ---------------------- updateAdPrice  ----------------------

    @Test
    void updateAdPrice_WhenPriceExists_UpdatesSuccessfully() throws BusinessRuleException, NotFoundException {
        // Arrange
        ArgumentCaptor<AdPriceEntity> captor = ArgumentCaptor.forClass(AdPriceEntity.class);
        AdPriceEntity updatedEntity = adPriceEntity();
        updatedEntity.setPrice(UPDATED_PRICE);

        when(adPriceRepository.findById(ID_PRICE)).thenReturn(Optional.of(adPriceEntity()));
        when(adPriceRepository.save(any())).thenReturn(updatedEntity);

        // Act
        AdPriceResponse result = adPriceService.updateAdPrice(ID_PRICE, adPriceUpdateRequest(), ID_ADMIN);

        // Assert
        assertAll(
                () -> verify(adPriceRepository).save(captor.capture()),
                () -> assertEquals(UPDATED_PRICE, captor.getValue().getPrice()),
                () -> assertEquals(ID_ADMIN,      captor.getValue().getUpdatedBy()),
                () -> assertEquals(UPDATED_PRICE, result.getPrice())
        );
    }

    @Test
    void updateAdPrice_WhenPriceNotFound_ThrowsNotFoundException() {
        // Arrange
        when(adPriceRepository.findById(ID_PRICE)).thenReturn(Optional.empty());

        // Act & Assert
        assertAll(
                () -> assertThrows(
                        NotFoundException.class,
                        () -> adPriceService.updateAdPrice(ID_PRICE, adPriceUpdateRequest(), ID_ADMIN)
                ),
                () -> verify(adPriceRepository, never()).save(any())
        );
    }

    //  ---------------------- deleteAdPrice  ----------------------

    @Test
    void deleteAdPrice_WhenPriceExists_DeletesSuccessfully() throws BusinessRuleException, NotFoundException {
        // Arrange
        when(adPriceRepository.existsById(ID_PRICE)).thenReturn(true);

        // Act
        adPriceService.deleteAdPrice(ID_PRICE);

        // Assert
        verify(adPriceRepository).deleteById(ID_PRICE);
    }

    @Test
    void deleteAdPrice_WhenPriceNotFound_ThrowsNotFoundException() {
        // Arrange
        when(adPriceRepository.existsById(ID_PRICE)).thenReturn(false);

        // Act & Assert
        assertAll(
                () -> assertThrows(
                        NotFoundException.class,
                        () -> adPriceService.deleteAdPrice(ID_PRICE)
                ),
                () -> verify(adPriceRepository, never()).deleteById(any())
        );
    }
}