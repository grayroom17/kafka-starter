package com.starter.kafka.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.starter.kafka.consumer.LibraryEventsConsumer;
import com.starter.kafka.entity.LibraryEvent;
import com.starter.kafka.repository.FailureRecordRepository;
import com.starter.kafka.repository.LibraryEventsRepository;
import lombok.experimental.NonFinal;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.IntegerDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.MessageListenerContainer;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.ContainerTestUtils;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.context.TestPropertySource;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static com.starter.kafka.entity.LibraryEventType.UPDATE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@EmbeddedKafka(topics = {
        "library-events",
        "library-events.RETRY",
        "library-events.DLT"},
        partitions = 3)
@TestPropertySource(properties = {
        "spring.kafka.producer.bootstrap-servers=${spring.embedded.kafka.brokers}",
        "spring.kafka.consumer.bootstrap-servers=${spring.embedded.kafka.brokers}",
        "kafka.retryListener.startup=false"
})
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class LibraryEventsConsumerIT {

    private static final Logger log = org.slf4j.LoggerFactory.getLogger(LibraryEventsConsumerIT.class);

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

    @Autowired
    FailureRecordRepository failureRecordRepository;

    @SpyBean
    LibraryEventsConsumer libraryEventsConsumer;

    @SpyBean
    LibraryEventsService libraryEventsService;

    @NonFinal
    @Value("${kafka.topics.retry}")
    String retryTopic;

    @NonFinal
    @Value("${kafka.topics.dlt}")
    String deadLetterTopic;

    Consumer<Integer, String> consumer;

    @BeforeEach
    void setUp() {
//        for (MessageListenerContainer messageListenerContainer : endpointRegistry.getListenerContainers()) {
//            ContainerTestUtils.waitForAssignment(messageListenerContainer, embeddedKafkaBroker.getPartitionsPerTopic());
//        }

        MessageListenerContainer container = endpointRegistry.getListenerContainers()
                .stream()
                .filter(messageListenerContainer -> Objects.equals(messageListenerContainer.getGroupId(), "library-events-listener-group"))
                .findFirst().get();

        ContainerTestUtils.waitForAssignment(container, embeddedKafkaBroker.getPartitionsPerTopic());
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

        Map<String, Object> configs = new HashMap<>(KafkaTestUtils.consumerProps("group2", "true", embeddedKafkaBroker));
        consumer = new DefaultKafkaConsumerFactory<>(configs, new IntegerDeserializer(), new StringDeserializer()).createConsumer();
        embeddedKafkaBroker.consumeFromAnEmbeddedTopic(consumer, deadLetterTopic);

        ConsumerRecord<Integer, String> consumerRecord = KafkaTestUtils.getSingleRecord(consumer, deadLetterTopic);
        log.info("consumerRecord is : {}", consumerRecord.value());
        assertEquals(json, consumerRecord.value());
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

        Map<String, Object> configs = new HashMap<>(KafkaTestUtils.consumerProps("group1", "true", embeddedKafkaBroker));
        consumer = new DefaultKafkaConsumerFactory<>(configs, new IntegerDeserializer(), new StringDeserializer()).createConsumer();
        embeddedKafkaBroker.consumeFromAnEmbeddedTopic(consumer, retryTopic);

        ConsumerRecord<Integer, String> consumerRecord = KafkaTestUtils.getSingleRecord(consumer, retryTopic);
        log.info("consumerRecord is : {}", consumerRecord.value());
        assertEquals(json, consumerRecord.value());
    }

    @Test
    void onMessage_update_validationFail_persistFailureRecordToDb() throws ExecutionException, InterruptedException, JsonProcessingException {
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

        assertEquals(1, failureRecordRepository.count());

        failureRecordRepository.findAll()
                .forEach(failureRecord -> log.info("failureRecord : {}", failureRecord));

    }

}