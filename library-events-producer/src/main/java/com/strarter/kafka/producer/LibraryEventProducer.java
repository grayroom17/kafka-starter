package com.strarter.kafka.producer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.strarter.kafka.domain.LibraryEvent;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@Slf4j
@Component
public class LibraryEventProducer {

    KafkaTemplate<Integer, String> kafkaTemplate;
    ObjectMapper objectMapper;

    @NonFinal
    @Value("${spring.kafka.template.default-topic}")
    public String topic;

    public CompletableFuture<SendResult<Integer, String>> sendLibraryEvent(LibraryEvent libraryEvent) throws JsonProcessingException {
        Integer key = libraryEvent.libraryEventId();
        String value = objectMapper.writeValueAsString(libraryEvent);

        CompletableFuture<SendResult<Integer, String>> completableFuture = kafkaTemplate.send(topic, key, value);

        return completableFuture.whenComplete((sendResult, throwable) -> {
            if (throwable != null) {
                handleError(throwable);
            } else {
                handleSuccess(key, value, sendResult);
            }
        });
    }

    public CompletableFuture<SendResult<Integer, String>> sendLibraryEventWithProducerRecord(LibraryEvent libraryEvent) throws JsonProcessingException {
        Integer key = libraryEvent.libraryEventId();
        String value = objectMapper.writeValueAsString(libraryEvent);

        ProducerRecord<Integer, String> producerRecord = buildProducerRecord(key, value);

        CompletableFuture<SendResult<Integer, String>> completableFuture = kafkaTemplate.send(producerRecord);

        return completableFuture.whenComplete((sendResult, throwable) -> {
            if (throwable != null) {
                handleError(throwable);
            } else {
                handleSuccess(key, value, sendResult);
            }
        });
    }

    public CompletableFuture<SendResult<Integer, String>> sendLibraryEventWithProducerRecordWithCustomHeaders(LibraryEvent libraryEvent) throws JsonProcessingException {
        Integer key = libraryEvent.libraryEventId();
        String value = objectMapper.writeValueAsString(libraryEvent);

        ProducerRecord<Integer, String> producerRecord = buildProducerRecordWithHeader(key, value);

        CompletableFuture<SendResult<Integer, String>> completableFuture = kafkaTemplate.send(producerRecord);

        return completableFuture.whenComplete((sendResult, throwable) -> {
            if (throwable != null) {
                handleError(throwable);
            } else {
                handleSuccess(key, value, sendResult);
            }
        });
    }

    private ProducerRecord<Integer, String> buildProducerRecord(Integer key, String value) {
        return new ProducerRecord<>(topic, key, value);
    }

    private ProducerRecord<Integer, String> buildProducerRecordWithHeader(Integer key, String value) {
        List<Header> headers = List.of(new RecordHeader("Authorities", ("Bearer " +
                                                                        "eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICJFTEFvbTN5RkFub1hvMW" +
                                                                        "RPREo5eDZDb0RSVHpTRU01NXNTYjBKLXJhR3pzIn0.eyJleHAiOjE2OTk1MTM4MjYsImlhd" +
                                                                        "CI6MTY5OTUxMjYyNiwianRpIjoiOTdmMDRjZTEtMGQ5Mi00NmFmLTk1M2ItYWQ5ZGNkMWVlN" +
                                                                        "GI1IiwiaXNzIjoiaHR0cHM6Ly9rZXljbG9hay5ub3ZhZW5lcmdpZXMubmV0Ojg0NDMvcmVhb" +
                                                                        "G1zL05vdmFQbGFudCIsImF1ZCI6ImFjY291bnQiLCJzdWIiOiJmNzFmZTExYS1hNjdiLTQ3N" +
                                                                        "WYtOTI0NS1kYzM1ODQ0NmY1YTIiLCJ0eXAiOiJCZWFyZXIiLCJhenAiOiJub3ZhcGxhbnQiL" +
                                                                        "CJzZXNzaW9uX3N0YXRlIjoiOTUyZTFlYzYtYmEwNi00ZGQ0LWJjM2EtODRiZDliNGU3ZTc5I" +
                                                                        "iwiYWNyIjoiMSIsImFsbG93ZWQtb3JpZ2lucyI6WyIqIl0sInJlYWxtX2FjY2VzcyI6eyJyb" +
                                                                        "2xlcyI6WyJvZmZsaW5lX2FjY2VzcyIsImRlZmF1bHQtcm9sZXMtbm92YXBsYW50IiwidW1hX" +
                                                                        "2F1dGhvcml6YXRpb24iXX0sInJlc291cmNlX2FjY2VzcyI6eyJhY2NvdW50Ijp7InJvbGVzI" +
                                                                        "jpbIm1hbmFnZS1hY2NvdW50IiwibWFuYWdlLWFjY291bnQtbGlua3MiLCJ2aWV3LXByb2Zpb" +
                                                                        "GUiXX19LCJzY29wZSI6ImVtYWlsIHByb2ZpbGUiLCJzaWQiOiI5NTJlMWVjNi1iYTA2LTRkZD" +
                                                                        "QtYmMzYS04NGJkOWI0ZTdlNzkiLCJlbWFpbF92ZXJpZmllZCI6ZmFsc2UsIm5hbWUiOiJBbm" +
                                                                        "RyZXkgQ2hlcHVyY2hlbmtvIiwicHJlZmVycmVkX3VzZXJuYW1lIjoiYW5kcmV5LmNoZXB1cm" +
                                                                        "NoZW5rbyIsImdpdmVuX25hbWUiOiJBbmRyZXkiLCJmYW1pbHlfbmFtZSI6IkNoZXB1cmNoZW" +
                                                                        "5rbyIsImVtYWlsIjoiYW5kcmV5LmNoZXB1cmNoZW5rb0BjYXNjZW5nLmNvbSJ9.wAyIaZaG5" +
                                                                        "6vfTu86ViSQtwtXTApeMQdg-VOEhOie8luu_rSEGCsCvi-axpS_ZMeIuFb5QPpSbxiP8wiP1" +
                                                                        "qc5EhJBlWdC0wzWkgHs7IMWpvD3p2jQyPi-UNkVfC9Bs13FdNURekFD9weZ3YSOOIBbpOcBX" +
                                                                        "yrVSEppxsv3dGUvVQX6lDV4s04VdKwCwbkHGtxXEb-YsDdC7L32N_pycmIOBT1ygnEPsmAjn" +
                                                                        "jBRpYjFg7nSlk7Xrf-qHGg6nRYbhvjkAWFFml-oB3MvOingkaSFcY6eNabTArVR1kHpbbVIc" +
                                                                        "j5TbNzoE-jqQ2CTmEC4Vj2SnL1TjPk1lBRZ0tfpgHH3aQ").getBytes()));

        return new ProducerRecord<>(topic, null, key, value, headers);
    }

    public SendResult<Integer, String> synchronousSendLibraryEvent(LibraryEvent libraryEvent)
            throws JsonProcessingException, ExecutionException, InterruptedException {
        Integer key = libraryEvent.libraryEventId();
        String value = objectMapper.writeValueAsString(libraryEvent);

        SendResult<Integer, String> sendResult = kafkaTemplate.send(topic, key, value).get();

        handleSuccess(key, value, sendResult);

        return sendResult;
    }

    private void handleSuccess(Integer key, String value, SendResult<Integer, String> sendResult) {
        log.info("Message sent successfully for the key : {} and the value : {} , partition is {} ",
                key, value, sendResult.getRecordMetadata().partition());
    }

    private void handleError(Throwable throwable) {
        log.error("Error sending the message and the exception is {} ", throwable.getMessage(), throwable);
    }

}
