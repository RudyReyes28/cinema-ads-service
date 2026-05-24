package com.codenbugs.adsservice.clients.wallet;

import com.codenbugs.adsservice.exceptions.BusinessRuleException;

import java.math.BigDecimal;
import java.util.UUID;

public interface WalletClient {
    void deduct(UUID advertiserId, BigDecimal amount, String token)
            throws BusinessRuleException;


}
