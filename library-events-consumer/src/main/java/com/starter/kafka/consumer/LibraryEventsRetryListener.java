package com.starter.kafka.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.starter.kafka.service.LibraryEventsService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@Slf4j
@Component
public class LibraryEventsRetryListener {

    LibraryEventsService libraryEventsService;

    @KafkaListener(topics = "${kafka.topics.retry}",
            groupId = "retry-listener-group",
            autoStartup = "${kafka.retryListener.startup:true}")
    public void onMessage(ConsumerRecord<Integer, String> consumerRecord) throws JsonProcessingException {
        log.info("ConsumerRecord in Retry Consumer: {}", consumerRecord);
        consumerRecord.headers()
                .forEach(header -> log.info("Key : {} , value : {} ", header.key(), new String(header.value())));
        libraryEventsService.processLibraryEvent(consumerRecord);
    }

}
