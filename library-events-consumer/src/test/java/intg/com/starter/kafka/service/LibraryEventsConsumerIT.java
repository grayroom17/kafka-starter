package com.starter.kafka.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.starter.kafka.consumer.LibraryEventsConsumer;
import com.starter.kafka.entity.LibraryEvent;
import com.starter.kafka.repository.LibraryEventsRepository;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.MessageListenerContainer;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.ContainerTestUtils;
import org.springframework.test.context.TestPropertySource;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static com.starter.kafka.entity.LibraryEventType.UPDATE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@EmbeddedKafka(topics = "library-events", partitions = 3)
@TestPropertySource(properties = {
        "spring.kafka.producer.bootstrap-servers=${spring.embedded.kafka.brokers}",
        "spring.kafka.consumer.bootstrap-servers=${spring.embedded.kafka.brokers}"
})
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class LibraryEventsConsumerIT {

    @Autowired
    EmbeddedKafkaBroker embeddedKafkaBroker;

    @Autowired
    KafkaTemplate<Integer, String> kafkaTemplate;

    @Autowired
    KafkaListenerEndpointRegistry endpointRegistry;

    @Autowired
    LibraryEventsRepository libraryEventsRepository;

    @Autowired
    ObjectMapper objectMapper;

    @SpyBean
    LibraryEventsConsumer libraryEventsConsumer;

    @SpyBean
    LibraryEventsService libraryEventsService;

    @BeforeEach
    void setUp() {
        for (MessageListenerContainer messageListenerContainer : endpointRegistry.getListenerContainers()) {
            ContainerTestUtils.waitForAssignment(messageListenerContainer, embeddedKafkaBroker.getPartitionsPerTopic());
        }
    }

    @AfterEach
    void tearDown() {
        libraryEventsRepository.deleteAll();
    }

    @Test
    void onMessage_creation() throws ExecutionException, InterruptedException, JsonProcessingException {
        String json = """
                {
                  "libraryEventId": null,
                  "libraryEventType": "NEW",
                  "book": {
                    "bookId": 456,
                    "bookName": "Kafka Using Spring Boot",
                    "bookAuthor": "Dilip"
                  }
                }""";
        kafkaTemplate.sendDefault(json).get();

        CountDownLatch latch = new CountDownLatch(1);
        latch.await(3, TimeUnit.SECONDS);

        verify(libraryEventsConsumer, times(1)).onMessage(isA(ConsumerRecord.class));
        verify(libraryEventsService, times(1)).processLibraryEvent(isA(ConsumerRecord.class));

        List<LibraryEvent> events = libraryEventsRepository.findAll();
        assertEquals(1, events.size());
        events.forEach(event -> {
            assertNotNull(event);
            assertEquals(456, event.getBook().getBookId());
        });
    }

    @Test
    void onMessage_update() throws ExecutionException, InterruptedException, JsonProcessingException {
        String json = """
                {
                  "libraryEventId": null,
                  "libraryEventType": "NEW",
                  "book": {
                    "bookId": 456,
                    "bookName": "Kafka Using Spring Boot",
                    "bookAuthor": "Dilip"
                  }
                }""";
        LibraryEvent libraryEvent = objectMapper.readValue(json, LibraryEvent.class);
        libraryEvent.getBook().setLibraryEvent(libraryEvent);
        libraryEventsRepository.save(libraryEvent);

        libraryEvent.setLibraryEventType(UPDATE);
        String newBookName = "Kafka Using Spring Boot 2.X";
        libraryEvent.getBook().setBookName(newBookName);
        String updatedJson = objectMapper.writeValueAsString(libraryEvent);

        kafkaTemplate.sendDefault(libraryEvent.getLibraryEventId(), updatedJson).get();

        CountDownLatch latch = new CountDownLatch(1);
        latch.await(3, TimeUnit.SECONDS);

        LibraryEvent event = libraryEventsRepository.findById(libraryEvent.getLibraryEventId()).get();
        assertEquals(newBookName, event.getBook().getBookName());
    }

    @Test
    void onMessage_update_validationFail() throws ExecutionException, InterruptedException, JsonProcessingException {
        String json = """
                {
                  "libraryEventId": null,
                  "libraryEventType": "UPDATE",
                  "book": {
                    "bookId": 456,
                    "bookName": "Kafka Using Spring Boot",
                    "bookAuthor": "Dilip"
                  }
                }""";
        kafkaTemplate.sendDefault(json).get();

        CountDownLatch latch = new CountDownLatch(1);
        latch.await(5, TimeUnit.SECONDS);

        verify(libraryEventsConsumer, times(1)).onMessage(isA(ConsumerRecord.class));
        verify(libraryEventsService, times(1)).processLibraryEvent(isA(ConsumerRecord.class));
    }

    @Test
    void onMessage_update_eventId999() throws ExecutionException, InterruptedException, JsonProcessingException {
        String json = """
                {
                  "libraryEventId": 999,
                  "libraryEventType": "UPDATE",
                  "book": {
                    "bookId": 456,
                    "bookName": "Kafka Using Spring Boot",
                    "bookAuthor": "Dilip"
                  }
                }""";
        kafkaTemplate.sendDefault(json).get();

        CountDownLatch latch = new CountDownLatch(1);
        latch.await(30, TimeUnit.SECONDS);

        verify(libraryEventsConsumer, times(6)).onMessage(isA(ConsumerRecord.class));
        verify(libraryEventsService, times(6)).processLibraryEvent(isA(ConsumerRecord.class));
    }

}