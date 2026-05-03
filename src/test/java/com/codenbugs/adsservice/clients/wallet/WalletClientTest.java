package com.codenbugs.adsservice.clients.wallet;

import com.codenbugs.adsservice.dtos.ads.DeductRequest;
import com.codenbugs.adsservice.exceptions.BusinessRuleException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class WalletClientTest {

    // -------------------- Constants ---------------------

    private static final UUID       ADVERTISER_ID = UUID.randomUUID();
    private static final BigDecimal AMOUNT        = new BigDecimal("10.00");
    private static final String     TOKEN         = "Bearer test-token";



    // --------------------
    // WalletClientMock
    // --------------------

    @Test
    void walletClientMock_WhenDeduct_DoesNotThrow() {
        WalletClientMock mock = new WalletClientMock();

        assertDoesNotThrow(() -> mock.deduct(ADVERTISER_ID, AMOUNT, TOKEN));
    }

    @Test
    void walletClientMock_WhenDeductWithNullAmount_DoesNotThrow() {
        WalletClientMock mock = new WalletClientMock();

        assertDoesNotThrow(() -> mock.deduct(ADVERTISER_ID, null, TOKEN));
    }

    @Test
    void walletClientMock_WhenDeductWithNullToken_DoesNotThrow() {
        WalletClientMock mock = new WalletClientMock();

        assertDoesNotThrow(() -> mock.deduct(ADVERTISER_ID, AMOUNT, null));
    }

    // --------------------
    // WalletClientHttp
    // --------------------

    @Mock
    private RestClient.Builder restClientBuilder;

    @Mock
    private RestClient restClient;

    @Mock
    private RestClient.RequestBodyUriSpec requestBodyUriSpec;

    @Mock
    private RestClient.RequestBodySpec requestBodySpec;

    @Mock
    private RestClient.ResponseSpec responseSpec;

    private WalletClientHttp walletClientHttp;
    @BeforeEach
    void setUp() {
        when(restClientBuilder.build()).thenReturn(restClient);
        walletClientHttp = new WalletClientHttp(restClientBuilder);
    }

    private void mockRestClientChain() {
        when(restClientBuilder.build()).thenReturn(restClient);
        when(restClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString(), any(UUID.class))).thenReturn(requestBodySpec);
        when(requestBodySpec.header(anyString(), anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.body(any(DeductRequest.class))).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);
    }

    @Test
    void walletClientHttp_WhenDeductSuccess_DoesNotThrow() throws Exception {

        when(restClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString(), any(UUID.class))).thenReturn(requestBodySpec);
        when(requestBodySpec.header(anyString(), anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.body(any(DeductRequest.class))).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.toBodilessEntity()).thenReturn(null);

        assertDoesNotThrow(() ->
                walletClientHttp.deduct(ADVERTISER_ID, AMOUNT, TOKEN)
        );
    }
    @Test
    void walletClientHttp_WhenInsufficientFunds_ThrowsBusinessRuleException() {

        when(restClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString(), any(UUID.class))).thenReturn(requestBodySpec);
        when(requestBodySpec.header(anyString(), anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.body(any(DeductRequest.class))).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);

        when(responseSpec.toBodilessEntity())
                .thenThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST));

        BusinessRuleException ex = assertThrows(
                BusinessRuleException.class,
                () -> walletClientHttp.deduct(ADVERTISER_ID, AMOUNT, TOKEN)
        );

        assertEquals("Saldo insuficiente", ex.getMessage());
    }

    @Test
    void walletClientHttp_WhenServerError_ThrowsBusinessRuleException() {

        when(restClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString(), any(UUID.class))).thenReturn(requestBodySpec);
        when(requestBodySpec.header(anyString(), anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.body(any(DeductRequest.class))).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);

        when(responseSpec.toBodilessEntity())
                .thenThrow(new HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR));

        BusinessRuleException ex = assertThrows(
                BusinessRuleException.class,
                () -> walletClientHttp.deduct(ADVERTISER_ID, AMOUNT, TOKEN)
        );

        assertEquals("Error al procesar el pago", ex.getMessage());
    }

    @Test
    void walletClientHttp_WhenUnauthorized_ThrowsBusinessRuleException() {

        when(restClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString(), any(UUID.class))).thenReturn(requestBodySpec);
        when(requestBodySpec.header(anyString(), anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.body(any(DeductRequest.class))).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);

        when(responseSpec.toBodilessEntity())
                .thenThrow(new HttpClientErrorException(HttpStatus.UNAUTHORIZED));

        BusinessRuleException ex = assertThrows(
                BusinessRuleException.class,
                () -> walletClientHttp.deduct(ADVERTISER_ID, AMOUNT, TOKEN)
        );

        assertEquals("Error al procesar el pago", ex.getMessage());
    }
}