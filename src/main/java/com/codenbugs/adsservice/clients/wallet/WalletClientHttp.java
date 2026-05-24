package com.codenbugs.adsservice.clients.wallet;

import com.codenbugs.adsservice.dtos.ads.DeductRequest;
import com.codenbugs.adsservice.exceptions.BusinessRuleException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.util.UUID;

@Component
@Profile("prod")
public class WalletClientHttp implements WalletClient {

    private final RestClient restClient;

    @Value("${services.users.url}")
    private String usersServiceUrl;

    public WalletClientHttp(RestClient.Builder builder) {
        this.restClient = builder.build();
    }

    @Override
    public void deduct(UUID advertiserId, BigDecimal amount, String token)
            throws BusinessRuleException {
        try {
            restClient.post()
                    .uri(usersServiceUrl + "/internal/wallets/{id}/deduct", advertiserId)
                    .header("Authorization", "Bearer " + token)
                    .body(new DeductRequest(amount))
                    .retrieve()
                    .toBodilessEntity();
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.BAD_REQUEST) {
                throw new BusinessRuleException("Saldo insuficiente");
            }
            throw new BusinessRuleException("Error al procesar el pago");
        }
    }
}