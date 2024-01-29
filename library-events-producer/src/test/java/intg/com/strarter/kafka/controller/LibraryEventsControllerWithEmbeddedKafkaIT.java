package com.strarter.kafka.controller;

import com.strarter.kafka.domain.LibraryEvent;
import com.strarter.kafka.util.TestUtil;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.assertEquals;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@ActiveProfiles("test")
@EmbeddedKafka(topics = "library-events")
@TestPropertySource(properties = {
        "spring.kafka.producer.bootstrap-servers=${spring.embedded.kafka.brokers}",
        "spring.kafka.admin.properties.bootstrap.servers=${spring.embedded.kafka.brokers}"
})
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class LibraryEventsControllerWithEmbeddedKafkaIT {

    @Autowired
    TestRestTemplate restTemplate;

    @Test
    void sendEventAsync() {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON.toString());
        HttpEntity<LibraryEvent> httpEntity = new HttpEntity<>(TestUtil.libraryEventRecord(), httpHeaders);

        ResponseEntity<LibraryEvent> response = restTemplate.exchange("/v1/async-event",
                HttpMethod.POST,
                httpEntity,
                LibraryEvent.class);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
    }

    @Test
    void sendEventAsyncWithProducerRecord() {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON.toString());
        HttpEntity<LibraryEvent> httpEntity = new HttpEntity<>(TestUtil.libraryEventRecord(), httpHeaders);

        ResponseEntity<LibraryEvent> response = restTemplate.exchange("/v1/async-event-with-producer-record",
                HttpMethod.POST,
                httpEntity,
                LibraryEvent.class);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
    }

    @Test
    void sendEventAsyncWithProducerRecordWithHeaders() {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON.toString());
        HttpEntity<LibraryEvent> httpEntity = new HttpEntity<>(TestUtil.libraryEventRecord(), httpHeaders);

        ResponseEntity<LibraryEvent> response = restTemplate.exchange("/v1/async-event-with-producer-record-with-custom-headers",
                HttpMethod.POST,
                httpEntity,
                LibraryEvent.class);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
    }

    @Test
    void sendEventSync() {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON.toString());
        HttpEntity<LibraryEvent> httpEntity = new HttpEntity<>(TestUtil.libraryEventRecord(), httpHeaders);

        ResponseEntity<LibraryEvent> response = restTemplate.exchange("/v1/sync-event",
                HttpMethod.POST,
                httpEntity,
                LibraryEvent.class);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
    }
}