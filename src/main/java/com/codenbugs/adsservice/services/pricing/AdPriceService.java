package com.codenbugs.adsservice.services.pricing;

import com.codenbugs.adsservice.dtos.pricing.AdPriceRequest;
import com.codenbugs.adsservice.dtos.pricing.AdPriceResponse;
import com.codenbugs.adsservice.dtos.pricing.AdPriceUpdateRequest;
import com.codenbugs.adsservice.exceptions.BusinessRuleException;
import com.codenbugs.adsservice.exceptions.NotFoundException;

import java.util.List;
import java.util.UUID;

public interface AdPriceService {

    List<AdPriceResponse> getAllAdPrices() throws BusinessRuleException;

    AdPriceResponse createAdPrice(AdPriceRequest request, UUID adminId) throws BusinessRuleException;

    AdPriceResponse updateAdPrice(UUID idPrice, AdPriceUpdateRequest request, UUID adminId) throws BusinessRuleException, NotFoundException;

    void deleteAdPrice(UUID idPrice) throws BusinessRuleException, NotFoundException;
}
