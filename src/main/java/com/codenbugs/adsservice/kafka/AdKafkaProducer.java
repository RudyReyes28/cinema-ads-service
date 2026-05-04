package com.codenbugs.adsservice.kafka;

import com.codenbugs.adsservice.events.AdCreatedEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class AdKafkaProducer {
    private static final String TOPIC = "ad.created";
    private final KafkaTemplate<String, AdCreatedEvent> kafkaTemplate;

    public AdKafkaProducer(KafkaTemplate<String, AdCreatedEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishAdCreatedEvent(AdCreatedEvent event) {
        kafkaTemplate.send(TOPIC, event);
    }
}
