package com.codenbugs.adsservice.dtos.ads;

import lombok.Value;

import java.util.List;
import java.util.UUID;

@Value
public class AdvertiserAds {
    UUID advertiserId;
    String advertiserName;
    Integer totalAds;
}
