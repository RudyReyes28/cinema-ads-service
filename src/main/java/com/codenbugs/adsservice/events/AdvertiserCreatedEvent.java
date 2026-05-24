package com.codenbugs.adsservice.events;

import com.codenbugs.adsservice.models.AdvertiserEntity;
import lombok.Value;

import java.util.UUID;

@Value
public class AdvertiserCreatedEvent {
    String event;
    String id;
    String name;
    String phone;

    public AdvertiserEntity toEntity (){
        AdvertiserEntity advertiser = new AdvertiserEntity();
        advertiser.setId(UUID.fromString(id));
        advertiser.setName(name);

        return advertiser;
    }
}
