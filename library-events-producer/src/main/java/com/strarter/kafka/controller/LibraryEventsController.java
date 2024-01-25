package com.strarter.kafka.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.strarter.kafka.domain.LibraryEvent;
import com.strarter.kafka.producer.LibraryEventProducer;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.ExecutionException;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@Slf4j
@RestController
@RequestMapping("/v1")
public class LibraryEventsController {

    LibraryEventProducer libraryEventProducer;

    @PostMapping("/async-event")
    ResponseEntity<LibraryEvent> sendEventAsync(@RequestBody LibraryEvent libraryEvent) throws JsonProcessingException {
        log.info("libraryEvent : {} ", libraryEvent);
        libraryEventProducer.sendLibraryEvent(libraryEvent);
        return ResponseEntity.status(HttpStatus.CREATED).body(libraryEvent);
    }

    @PostMapping("/async-event-with-producer-record")
    ResponseEntity<LibraryEvent> sendEventAsyncWithProducerRecord(@RequestBody LibraryEvent libraryEvent) throws JsonProcessingException {
        log.info("libraryEvent : {} ", libraryEvent);
        libraryEventProducer.sendLibraryEventWithProducerRecord(libraryEvent);
        return ResponseEntity.status(HttpStatus.CREATED).body(libraryEvent);
    }

    @PostMapping("/async-event-with-producer-record-with-custom-headers")
    ResponseEntity<LibraryEvent> sendEventAsyncWithProducerRecordWithHeaders(@RequestBody LibraryEvent libraryEvent) throws JsonProcessingException {
        log.info("libraryEvent : {} ", libraryEvent);
        libraryEventProducer.sendLibraryEventWithProducerRecordWithCustomHeaders(libraryEvent);
        return ResponseEntity.status(HttpStatus.CREATED).body(libraryEvent);
    }

    @PostMapping("/sync-event")
    ResponseEntity<LibraryEvent> sendEventSync(@RequestBody LibraryEvent libraryEvent)
            throws JsonProcessingException, ExecutionException, InterruptedException {
        log.info("libraryEvent : {} ", libraryEvent);
        libraryEventProducer.synchronousSendLibraryEvent(libraryEvent);
        return ResponseEntity.status(HttpStatus.CREATED).body(libraryEvent);
    }

}
