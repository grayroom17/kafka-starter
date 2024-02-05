package com.starter.kafka.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.starter.kafka.service.LibraryEventsService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.Header;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@Slf4j
@Component
public class LibraryEventsConsumer {

    LibraryEventsService libraryEventsService;

    @KafkaListener(topics = "library-events")
    public void onMessage(ConsumerRecord<Integer, String> consumerRecord) throws JsonProcessingException {
        Header authoritiesHeader = consumerRecord.headers().lastHeader("Authorities");
        if (authoritiesHeader != null) {
            String authorities = new String(authoritiesHeader.value());
            log.info("Token : {}", authorities);
        }

        log.info("ConsumerRecord : {}", consumerRecord);
        libraryEventsService.processLibraryEvent(consumerRecord);
    }

}
