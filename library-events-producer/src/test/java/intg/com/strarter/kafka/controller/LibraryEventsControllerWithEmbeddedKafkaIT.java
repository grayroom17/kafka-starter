package com.strarter.kafka.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.strarter.kafka.domain.LibraryEvent;
import com.strarter.kafka.util.TestUtil;
import lombok.experimental.NonFinal;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.serialization.IntegerDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;

@EmbeddedKafka(topics = "library-events")
@TestPropertySource(properties = {
        "spring.kafka.producer.bootstrap-servers=${spring.embedded.kafka.brokers}",
        "spring.kafka.admin.properties.bootstrap.servers=${spring.embedded.kafka.brokers}"
})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class LibraryEventsControllerWithEmbeddedKafkaIT {

    @Autowired
    TestRestTemplate restTemplate;
    @Autowired
    EmbeddedKafkaBroker kafkaBroker;
    @Autowired
    ObjectMapper objectMapper;

    private Consumer<Integer, String> consumer;

    @BeforeEach
    void setUp() {
        HashMap<String, Object> configs = new HashMap<>(KafkaTestUtils.consumerProps("group1", "true", kafkaBroker));
        configs.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
        consumer = new DefaultKafkaConsumerFactory<>(configs, new IntegerDeserializer(), new StringDeserializer()).createConsumer();
        kafkaBroker.consumeFromAllEmbeddedTopics(consumer);
    }

    @AfterEach
    void tearDown() {
        consumer.close();
    }

    @Test
    void sendEventAsync() {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON.toString());
        LibraryEvent expected = TestUtil.libraryEventRecord();
        HttpEntity<LibraryEvent> httpEntity = new HttpEntity<>(expected, httpHeaders);

        ResponseEntity<LibraryEvent> response = restTemplate.exchange("/v1/async-event",
                HttpMethod.POST,
                httpEntity,
                LibraryEvent.class);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());

        ConsumerRecords<Integer, String> consumerRecords = KafkaTestUtils.getRecords(consumer);

        assertEquals(1, consumerRecords.count());
        consumerRecords.forEach(record -> {
            LibraryEvent actual = TestUtil.parseLibraryEventRecord(objectMapper, record.value());
            assertEquals(expected, actual);
        });
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