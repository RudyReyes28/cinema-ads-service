package com.codenbugs.adsservice.clients.wallet;

import com.codenbugs.adsservice.exceptions.BusinessRuleException;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;

@Component
@Profile({"dev", "test"})
public class WalletClientMock implements WalletClient {
    @Override
    public void deduct(UUID advertiserId, BigDecimal amount, String token)
            throws BusinessRuleException {
        // Simula exito
    }
}