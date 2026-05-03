package com.codenbugs.adsservice.services.ads.creators;

import com.codenbugs.adsservice.dtos.ads.AdRequest;
import com.codenbugs.adsservice.enums.AdPeriod;
import com.codenbugs.adsservice.enums.AdType;
import com.codenbugs.adsservice.exceptions.BusinessRuleException;
import com.codenbugs.adsservice.exceptions.FileStorageException;
import com.codenbugs.adsservice.models.AdEntity;
import com.codenbugs.adsservice.models.AdPriceEntity;
import com.codenbugs.adsservice.models.AdvertiserEntity;
import com.codenbugs.adsservice.services.storage.FileStorageService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AdCreatorTest {

    // ------------------- Constants -------------------

    private static final String  CONTENT_TEXT = "Este es el texto del anuncio";
    private static final String  VIDEO_URL    = "https://youtube.com/watch?v=abc123";
    private static final String  IMAGE_S3_URL = "https://bucket.s3.us-east-1.amazonaws.com/ads/images/uuid-img.jpg";
    private static final String  VIDEO_S3_URL = "https://bucket.s3.us-east-1.amazonaws.com/ads/videos/uuid-vid.mp4";
    private static final BigDecimal PRICE     = new BigDecimal("10.00");

    // ------------------- Mocks -------------------

    @Mock
    private FileStorageService fileStorageService;

    @InjectMocks
    private TextAdCreator  textAdCreator;

    @InjectMocks
    private ImageAdCreator imageAdCreator;

    @InjectMocks
    private VideoAdCreator videoAdCreator;

    // ------------------- Helpers -------------------

    private AdPriceEntity adPrice(AdType type, AdPeriod period) {
        AdPriceEntity price = new AdPriceEntity();
        price.setId(UUID.randomUUID());
        price.setAdType(type);
        price.setAdPeriod(period);
        price.setPrice(PRICE);
        return price;
    }

    private AdvertiserEntity advertiser() {
        AdvertiserEntity advertiser = new AdvertiserEntity();
        advertiser.setId(UUID.randomUUID());
        return advertiser;
    }

    private AdRequest requestWithText() {
        AdRequest request = new AdRequest(UUID.randomUUID(),CONTENT_TEXT, null );
        return request;
    }

    private AdRequest requestWithVideoUrl() {
        AdRequest request = new AdRequest(UUID.randomUUID(), CONTENT_TEXT, VIDEO_URL);
        return request;
    }

    private AdRequest requestWithBlankText() {
        AdRequest request = new AdRequest(UUID.randomUUID(), "   ", null);
        return request;
    }

    private MultipartFile mockImage() {
        return new MockMultipartFile("image", "test.jpg", "image/jpeg", "fake-image".getBytes());
    }

    private MultipartFile mockVideo() {
        return new MockMultipartFile("videoFile", "test.mp4", "video/mp4", "fake-video".getBytes());
    }

    // -------------------
    // TextAdCreator
    // -------------------

    @Test
    void textAdCreator_supportedType_ReturnsText() {
        assertEquals(AdType.TEXT, textAdCreator.supportedType());
    }

    @Test
    void textAdCreator_WhenValidRequest_CreatesAdCorrectly() throws BusinessRuleException, FileStorageException {
        AdPriceEntity price = adPrice(AdType.TEXT, AdPeriod.ONE_DAY);

        AdEntity result = textAdCreator.createAd(requestWithText(), price, advertiser(), null, null);

        assertAll(
                () -> assertNotNull(result),
                () -> assertEquals(CONTENT_TEXT,    result.getContentText()),
                () -> assertEquals(AdType.TEXT,     result.getAdType()),
                () -> assertEquals(AdPeriod.ONE_DAY, result.getAdPeriod()),
                () -> assertEquals(PRICE,           result.getAmountPaid()),
                () -> assertFalse(result.isActive()),
                () -> assertNotNull(result.getStartDate()),
                () -> assertNotNull(result.getEndDate()),
                () -> assertTrue(result.getEndDate().isAfter(result.getStartDate())),
                () -> assertNull(result.getImageUrl()),
                () -> assertNull(result.getVideoUrl())
        );
    }

    @Test
    void textAdCreator_WhenContentTextIsBlank_ThrowsBusinessRuleException() {
        AdPriceEntity price = adPrice(AdType.TEXT, AdPeriod.ONE_DAY);

        assertThrows(BusinessRuleException.class,
                () -> textAdCreator.createAd(requestWithBlankText(), price, advertiser(), null, null)
        );
    }

    @Test
    void textAdCreator_WhenContentTextIsNull_ThrowsBusinessRuleException() {
        AdRequest request = new AdRequest(UUID.randomUUID(), null, null);
        AdPriceEntity price = adPrice(AdType.TEXT, AdPeriod.ONE_DAY);

        assertThrows(BusinessRuleException.class,
                () -> textAdCreator.createAd(request, price, advertiser(), null, null)
        );
    }

    @Test
    void textAdCreator_EndDate_IsCalculatedCorrectlyByPeriod() throws BusinessRuleException, FileStorageException {
        AdPriceEntity price = adPrice(AdType.TEXT, AdPeriod.ONE_WEEK);

        AdEntity result = textAdCreator.createAd(requestWithText(), price, advertiser(), null, null);

        long days = java.time.temporal.ChronoUnit.DAYS.between(result.getStartDate(), result.getEndDate());
        assertEquals(AdPeriod.ONE_WEEK.getDays(), days);
    }

    // -------------------
    // ImageAdCreator
    // -------------------

    @Test
    void imageAdCreator_supportedType_ReturnsTextImage() {
        assertEquals(AdType.TEXT_IMAGE, imageAdCreator.supportedType());
    }

    @Test
    void imageAdCreator_WhenValidRequest_CreatesAdCorrectly() throws BusinessRuleException, FileStorageException {
        AdPriceEntity price = adPrice(AdType.TEXT_IMAGE, AdPeriod.THREE_DAYS);
        when(fileStorageService.uploadFile(any(), eq("ads/images"))).thenReturn(IMAGE_S3_URL);

        AdEntity result = imageAdCreator.createAd(requestWithText(), price, advertiser(), mockImage(), null);

        assertAll(
                () -> assertNotNull(result),
                () -> assertEquals(CONTENT_TEXT,       result.getContentText()),
                () -> assertEquals(AdType.TEXT_IMAGE,  result.getAdType()),
                () -> assertEquals(IMAGE_S3_URL,       result.getImageUrl()),
                () -> assertEquals(PRICE,              result.getAmountPaid()),
                () -> assertFalse(result.isActive()),
                () -> assertNull(result.getVideoUrl())
        );
        verify(fileStorageService).uploadFile(any(), eq("ads/images"));
    }

    @Test
    void imageAdCreator_WhenImageIsNull_ThrowsBusinessRuleException() {
        AdPriceEntity price = adPrice(AdType.TEXT_IMAGE, AdPeriod.ONE_DAY);

        assertThrows(BusinessRuleException.class,
                () -> imageAdCreator.createAd(requestWithText(), price, advertiser(), null, null)
        );
        verifyNoInteractions(fileStorageService);
    }

    @Test
    void imageAdCreator_WhenImageIsEmpty_ThrowsBusinessRuleException() {
        AdPriceEntity price = adPrice(AdType.TEXT_IMAGE, AdPeriod.ONE_DAY);
        MultipartFile emptyImage = new MockMultipartFile("image", new byte[0]);

        assertThrows(BusinessRuleException.class,
                () -> imageAdCreator.createAd(requestWithText(), price, advertiser(), emptyImage, null)
        );
        verifyNoInteractions(fileStorageService);
    }

    @Test
    void imageAdCreator_WhenContentTextIsBlank_ThrowsBusinessRuleException() {
        AdPriceEntity price = adPrice(AdType.TEXT_IMAGE, AdPeriod.ONE_DAY);

        assertThrows(BusinessRuleException.class,
                () -> imageAdCreator.createAd(requestWithBlankText(), price, advertiser(), mockImage(), null)
        );
        verifyNoInteractions(fileStorageService);
    }

    @Test
    void imageAdCreator_WhenStorageFails_ThrowsFileStorageException() throws FileStorageException {
        AdPriceEntity price = adPrice(AdType.TEXT_IMAGE, AdPeriod.ONE_DAY);
        when(fileStorageService.uploadFile(any(), any())).thenThrow(new FileStorageException("S3 error"));

        assertThrows(FileStorageException.class,
                () -> imageAdCreator.createAd(requestWithText(), price, advertiser(), mockImage(), null)
        );
    }

    // -------------------
    // VideoAdCreator
    // -------------------

    @Test
    void videoAdCreator_supportedType_ReturnsVideoText() {
        assertEquals(AdType.VIDEO_TEXT, videoAdCreator.supportedType());
    }

    @Test
    void videoAdCreator_WhenVideoUrl_CreatesAdCorrectly() throws BusinessRuleException, FileStorageException {
        AdPriceEntity price = adPrice(AdType.VIDEO_TEXT, AdPeriod.ONE_WEEK);

        AdEntity result = videoAdCreator.createAd(requestWithVideoUrl(), price, advertiser(), null, null);

        assertAll(
                () -> assertNotNull(result),
                () -> assertEquals(CONTENT_TEXT,      result.getContentText()),
                () -> assertEquals(AdType.VIDEO_TEXT, result.getAdType()),
                () -> assertEquals(VIDEO_URL,         result.getVideoUrl()),
                () -> assertEquals(PRICE,             result.getAmountPaid()),
                () -> assertFalse(result.isActive()),
                () -> assertNull(result.getImageUrl())
        );
        verifyNoInteractions(fileStorageService);
    }

    @Test
    void videoAdCreator_WhenVideoFile_UploadsAndCreatesAdCorrectly() throws BusinessRuleException, FileStorageException {
        AdPriceEntity price = adPrice(AdType.VIDEO_TEXT, AdPeriod.TWO_WEEKS);
        when(fileStorageService.uploadFile(any(), eq("ads/videos"))).thenReturn(VIDEO_S3_URL);

        AdEntity result = videoAdCreator.createAd(requestWithText(), price, advertiser(), null, mockVideo());

        assertAll(
                () -> assertNotNull(result),
                () -> assertEquals(VIDEO_S3_URL, result.getVideoUrl()),
                () -> assertFalse(result.isActive())
        );
        verify(fileStorageService).uploadFile(any(), eq("ads/videos"));
    }

    @Test
    void videoAdCreator_WhenBothFileAndUrlAreNull_ThrowsBusinessRuleException() {
        AdPriceEntity price = adPrice(AdType.VIDEO_TEXT, AdPeriod.ONE_DAY);

        assertThrows(BusinessRuleException.class,
                () -> videoAdCreator.createAd(requestWithText(), price, advertiser(), null, null)
        );
        verifyNoInteractions(fileStorageService);
    }

    @Test
    void videoAdCreator_WhenContentTextIsBlank_ThrowsBusinessRuleException() {
        AdPriceEntity price = adPrice(AdType.VIDEO_TEXT, AdPeriod.ONE_DAY);

        assertThrows(BusinessRuleException.class,
                () -> videoAdCreator.createAd(requestWithBlankText(), price, advertiser(), null, mockVideo())
        );
        verifyNoInteractions(fileStorageService);
    }

    @Test
    void videoAdCreator_WhenStorageFails_ThrowsFileStorageException() throws FileStorageException {
        AdPriceEntity price = adPrice(AdType.VIDEO_TEXT, AdPeriod.ONE_DAY);
        when(fileStorageService.uploadFile(any(), any())).thenThrow(new FileStorageException("S3 error"));

        assertThrows(FileStorageException.class,
                () -> videoAdCreator.createAd(requestWithText(), price, advertiser(), null, mockVideo())
        );
    }

    // -------------------
    // AdCreatorFactory
    // -------------------

    @Test
    void adCreatorFactory_WhenTextType_ReturnsTextAdCreator() throws BusinessRuleException {
        AdCreatorFactory factory = new AdCreatorFactory(
                List.of(textAdCreator, imageAdCreator, videoAdCreator)
        );

        AdCreator creator = factory.getCreator(AdType.TEXT);

        assertInstanceOf(TextAdCreator.class, creator);
    }

    @Test
    void adCreatorFactory_WhenTextImageType_ReturnsImageAdCreator() throws BusinessRuleException {
        AdCreatorFactory factory = new AdCreatorFactory(
                List.of(textAdCreator, imageAdCreator, videoAdCreator)
        );

        AdCreator creator = factory.getCreator(AdType.TEXT_IMAGE);

        assertInstanceOf(ImageAdCreator.class, creator);
    }

    @Test
    void adCreatorFactory_WhenVideoTextType_ReturnsVideoAdCreator() throws BusinessRuleException {
        AdCreatorFactory factory = new AdCreatorFactory(
                List.of(textAdCreator, imageAdCreator, videoAdCreator)
        );

        AdCreator creator = factory.getCreator(AdType.VIDEO_TEXT);

        assertInstanceOf(VideoAdCreator.class, creator);
    }

    @Test
    void adCreatorFactory_WhenUnsupportedType_ThrowsBusinessRuleException() {
        AdCreatorFactory factory = new AdCreatorFactory(List.of());

        assertThrows(BusinessRuleException.class,
                () -> factory.getCreator(AdType.TEXT)
        );
    }
}