package com.codenbugs.adsservice.kafka;

import com.codenbugs.adsservice.events.AdCreatedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class AdKafkaProducer {
    private static final String TOPIC = "ad.created";
    private final KafkaTemplate<String, AdCreatedEvent> kafkaTemplate;

    public AdKafkaProducer(KafkaTemplate<String, AdCreatedEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    private void publish(String topic, AdCreatedEvent event) {
        try {
            kafkaTemplate.send(topic, event).get();
        } catch (Exception e) {
            log.error("Error al publicar el evento en topic {}:{} ", topic, e.getMessage());

        }
    }

    public void publishAdCreatedEvent(AdCreatedEvent event) {
        kafkaTemplate.send(TOPIC, event);
    }
}
