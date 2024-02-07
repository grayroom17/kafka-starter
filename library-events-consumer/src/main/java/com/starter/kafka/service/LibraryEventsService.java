package com.starter.kafka.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.starter.kafka.entity.LibraryEvent;
import com.starter.kafka.repository.LibraryEventsRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.dao.RecoverableDataAccessException;
import org.springframework.stereotype.Service;

import static com.starter.kafka.entity.LibraryEventType.NEW;
import static com.starter.kafka.entity.LibraryEventType.UPDATE;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@Slf4j
@Service
public class LibraryEventsService {

    LibraryEventsRepository libraryEventsRepository;
    ObjectMapper objectMapper;

    public void processLibraryEvent(ConsumerRecord<Integer, String> consumerRecord) throws JsonProcessingException {
        LibraryEvent libraryEvent = objectMapper.readValue(consumerRecord.value(), LibraryEvent.class);
        log.info("libraryEvent : {}", libraryEvent);
        validate(libraryEvent);
        save(libraryEvent);
    }

    private void validate(LibraryEvent libraryEvent) {
        if (libraryEvent != null && libraryEvent.getLibraryEventId().equals(999)) {
            throw new RecoverableDataAccessException("Temporary Network Issue");
        }
        if (libraryEvent.getLibraryEventType().equals(NEW)) {
            if (libraryEvent.getLibraryEventId() != null) {
                throw new IllegalArgumentException("LibraryEvent id should be null when create new LibraryEvent");
            }
            return;
        }
        if (libraryEvent.getLibraryEventType().equals(UPDATE)) {
            if (libraryEvent.getLibraryEventId() == null) {
                throw new IllegalArgumentException("LibraryEvent id should be not null when update LibraryEvent");
            }
            libraryEventsRepository.findById(libraryEvent.getLibraryEventId())
                    .orElseThrow(() -> new IllegalArgumentException("Not a valid LibraryEvent"));
            log.info("Validation is successful for the LibraryEvent : {}", libraryEvent);
            return;
        }
        throw new IllegalArgumentException("Invalid LibraryEvent Type");
    }

    private void save(LibraryEvent libraryEvent) {
        libraryEvent.getBook().setLibraryEvent(libraryEvent);
        libraryEventsRepository.save(libraryEvent);
        log.info("Successfully Persisted the library event : {}", libraryEvent);
    }

}
