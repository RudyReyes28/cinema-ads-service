package com.codenbugs.adsservice.controllers.pricing;

import com.codenbugs.adsservice.dtos.pricing.AdPriceRequest;
import com.codenbugs.adsservice.dtos.pricing.AdPriceResponse;
import com.codenbugs.adsservice.dtos.pricing.AdPriceUpdateRequest;
import com.codenbugs.adsservice.enums.AdPeriod;
import com.codenbugs.adsservice.enums.AdType;
import com.codenbugs.adsservice.exceptions.BusinessRuleException;
import com.codenbugs.adsservice.exceptions.GlobalExceptionHandler;
import com.codenbugs.adsservice.exceptions.NotFoundException;
import com.codenbugs.adsservice.services.pricing.AdPriceService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdPriceController.class)
@Import(GlobalExceptionHandler.class)
public class AdPriceControllerTest {

    // ------------------------ Constants ------------------------

    private static final UUID        ID_PRICE      = UUID.randomUUID();
    private static final UUID        ID_ADMIN      = UUID.randomUUID();
    private static final BigDecimal  PRICE         = new BigDecimal("10.0");
    private static final BigDecimal  UPDATED_PRICE = new BigDecimal("25.0");
    private static final AdType      AD_TYPE       = AdType.TEXT;
    private static final AdPeriod    AD_PERIOD     = AdPeriod.ONE_DAY;

    private static final String BASE_URL      = "/api/v1/ad-prices";
    private static final String BY_ID_URL     = "/api/v1/ad-prices/{idPrice}";

    // ------------------------ Autowired ------------------------

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AdPriceService adPriceService;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    // ------------------------ Helpers ------------------------

    private AdPriceResponse adPriceResponse() {
        return new AdPriceResponse(
                ID_PRICE,
                AD_TYPE.name(),
                AD_PERIOD.name(),
                PRICE,
                LocalDateTime.now(),
                ID_ADMIN
        );
    }

    private AdPriceRequest adPriceRequest() {
        return new AdPriceRequest(AD_TYPE, AD_PERIOD, PRICE);
    }

    private AdPriceUpdateRequest adPriceUpdateRequest() {
        return new AdPriceUpdateRequest(UPDATED_PRICE);
    }

    private SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor systemAdminJwt() {
        return jwt()
                .jwt(token -> token
                        .subject(ID_ADMIN.toString())
                        .claim("roles", List.of("SYSTEM_ADMIN"))
                );
    }

    private SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor advertiserJwt() {
        return jwt()
                .jwt(token -> token
                        .subject(UUID.randomUUID().toString())
                        .claim("roles", List.of("ADVERTISER"))
                );
    }

    //------------------------POST /api/v1/ad-prices ------------------------

    @Test
    void createAdPrice_WhenValidRequest_Returns201() throws Exception {
        when(adPriceService.createAdPrice(any(AdPriceRequest.class), any(UUID.class)))
                .thenReturn(adPriceResponse());

        mockMvc.perform(post(BASE_URL)
                        .with(systemAdminJwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(adPriceRequest())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(ID_PRICE.toString()))
                .andExpect(jsonPath("$.adType").value(AD_TYPE.name()))
                .andExpect(jsonPath("$.adPeriod").value(AD_PERIOD.name()));

        verify(adPriceService).createAdPrice(any(AdPriceRequest.class), eq(ID_ADMIN));
    }

    @Test
    void createAdPrice_WhenCombinationAlreadyExists_Returns400() throws Exception {
        when(adPriceService.createAdPrice(any(AdPriceRequest.class), any(UUID.class)))
                .thenThrow(new BusinessRuleException("Ya existe un precio para el tipo de anuncio y periodo especificados"));

        mockMvc.perform(post(BASE_URL)
                        .with(systemAdminJwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(adPriceRequest())))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createAdPrice_WhenRequestBodyIsInvalid_Returns400() throws Exception {
        AdPriceRequest invalidRequest = new AdPriceRequest(null, null, null);

        mockMvc.perform(post(BASE_URL)
                        .with(systemAdminJwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(adPriceService, never()).createAdPrice(any(), any());
    }

    //------------------------ GET /api/v1/ad-prices ------------------------

    @Test
    void getAllAdPrices_WhenPricesExist_Returns200() throws Exception {
        when(adPriceService.getAllAdPrices()).thenReturn(List.of(adPriceResponse()));

        mockMvc.perform(get(BASE_URL)
                        .with(systemAdminJwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(ID_PRICE.toString()))
                .andExpect(jsonPath("$[0].adType").value(AD_TYPE.name()))
                .andExpect(jsonPath("$[0].adPeriod").value(AD_PERIOD.name()))
                .andExpect(jsonPath("$[0].price").value(PRICE));

        verify(adPriceService).getAllAdPrices();
    }

    @Test
    void getAllAdPrices_WhenCalledByAdvertiser_Returns200() throws Exception {
        when(adPriceService.getAllAdPrices()).thenReturn(List.of(adPriceResponse()));

        mockMvc.perform(get(BASE_URL)
                        .with(advertiserJwt()))
                .andExpect(status().isOk());

        verify(adPriceService).getAllAdPrices();
    }

    @Test
    void getAllAdPrices_WhenNoPricesExist_ReturnsEmptyList() throws Exception {
        when(adPriceService.getAllAdPrices()).thenReturn(List.of());

        mockMvc.perform(get(BASE_URL)
                        .with(systemAdminJwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    // ------------------------ PUT /api/v1/ad-prices/{idPrice} ------------------------

    @Test
    void updateAdPrice_WhenValidRequest_Returns200() throws Exception {
        AdPriceResponse updatedResponse = new AdPriceResponse(
                ID_PRICE, AD_TYPE.name(), AD_PERIOD.name(), UPDATED_PRICE, LocalDateTime.now(), ID_ADMIN
        );
        when(adPriceService.updateAdPrice(eq(ID_PRICE), any(AdPriceUpdateRequest.class), any(UUID.class)))
                .thenReturn(updatedResponse);

        mockMvc.perform(put(BY_ID_URL, ID_PRICE)
                        .with(systemAdminJwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(adPriceUpdateRequest())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(ID_PRICE.toString()))
                .andExpect(jsonPath("$.price").value(UPDATED_PRICE));

        verify(adPriceService).updateAdPrice(eq(ID_PRICE), any(AdPriceUpdateRequest.class), eq(ID_ADMIN));
    }

    @Test
    void updateAdPrice_WhenPriceNotFound_Returns404() throws Exception {
        when(adPriceService.updateAdPrice(eq(ID_PRICE), any(AdPriceUpdateRequest.class), any(UUID.class)))
                .thenThrow(new NotFoundException("Precio de la publicidad no encontrada"));

        mockMvc.perform(put(BY_ID_URL, ID_PRICE)
                        .with(systemAdminJwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(adPriceUpdateRequest())))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateAdPrice_WhenRequestBodyIsInvalid_Returns400() throws Exception {
        AdPriceUpdateRequest invalidRequest = new AdPriceUpdateRequest(null);

        mockMvc.perform(put(BY_ID_URL, ID_PRICE)
                        .with(systemAdminJwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(adPriceService, never()).updateAdPrice(any(), any(), any());
    }

    // ------------------------ DELETE /api/v1/ad-prices/{idPrice} ------------------------

    @Test
    void deleteAdPrice_WhenPriceExists_Returns204() throws Exception {
        doNothing().when(adPriceService).deleteAdPrice(ID_PRICE);

        mockMvc.perform(delete(BY_ID_URL, ID_PRICE)
                        .with(systemAdminJwt()))
                .andExpect(status().isNoContent());

        verify(adPriceService).deleteAdPrice(ID_PRICE);
    }

    @Test
    void deleteAdPrice_WhenPriceNotFound_Returns404() throws Exception {
        doThrow(new NotFoundException("Precio de la publicidad no encontrada"))
                .when(adPriceService).deleteAdPrice(ID_PRICE);

        mockMvc.perform(delete(BY_ID_URL, ID_PRICE)
                        .with(systemAdminJwt()))
                .andExpect(status().isNotFound());
    }
}
