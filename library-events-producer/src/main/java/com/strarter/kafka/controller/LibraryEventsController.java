package com.strarter.kafka.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.strarter.kafka.domain.LibraryEvent;
import com.strarter.kafka.producer.LibraryEventProducer;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.ExecutionException;

import static com.strarter.kafka.domain.LibraryEventType.UPDATE;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@Slf4j
@RestController
@RequestMapping("/v1")
public class LibraryEventsController {

    LibraryEventProducer libraryEventProducer;

    @PostMapping("/async-event")
    ResponseEntity<LibraryEvent> sendEventAsync(@RequestBody @Valid LibraryEvent libraryEvent) throws JsonProcessingException {
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

    @PutMapping("/update-event")
    public ResponseEntity<?> updateLibraryEvent(@RequestBody @Valid LibraryEvent libraryEvent) throws JsonProcessingException {
        log.info("libraryEvent : {} ", libraryEvent);

//        if (libraryEvent.libraryEventId() == null) {
//            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Please pass the LibraryEventId");
//        }

        if (!libraryEvent.libraryEventType().equals(UPDATE)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Only UPDATE event type is supported");
        }

        libraryEventProducer.sendLibraryEvent(libraryEvent);
        return ResponseEntity.status(HttpStatus.OK).body(libraryEvent);
    }

}
