package com.codenbugs.adsservice.kafka;

import com.codenbugs.adsservice.events.AdvertiserCreatedEvent;
import com.codenbugs.adsservice.repositories.AdvertiserRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.KafkaListeners;
import org.springframework.stereotype.Service;

@Service
public class AdvertiserKafkaConsumer {

    private final AdvertiserRepository advertiserRepository;
    private final ObjectMapper objectMapper;

    private static final String USER_EVENTS_TOPIC = "user-events";


    public AdvertiserKafkaConsumer(AdvertiserRepository advertiserRepository, ObjectMapper objectMapper) {
        this.advertiserRepository = advertiserRepository;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = USER_EVENTS_TOPIC, groupId = "ads-service-group")
    public void onAdvertiserCreated(String payload) throws JsonProcessingException {
        AdvertiserCreatedEvent event = objectMapper.readValue(payload, AdvertiserCreatedEvent.class);
        if ("ADVERTISER_CREATED".equals(event.getEvent())) {
            advertiserRepository.save(event.toEntity());
        }
    }
}
