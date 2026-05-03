package com.codenbugs.adsservice.services.ads.creators;

import com.codenbugs.adsservice.enums.AdType;
import com.codenbugs.adsservice.exceptions.BusinessRuleException;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class AdCreatorFactory {

    private final Map<AdType, AdCreator> creators;

    public AdCreatorFactory(List<AdCreator> creatorList) {
        this.creators = creatorList.stream()
                .collect(Collectors.toMap(AdCreator::supportedType, c -> c));
    }

    public AdCreator getCreator(AdType adType) throws BusinessRuleException {
        AdCreator creator = creators.get(adType);
        if (creator == null) {
            throw new BusinessRuleException("Tipo de anuncio no soportado: " + adType);
        }
        return creator;
    }
}
