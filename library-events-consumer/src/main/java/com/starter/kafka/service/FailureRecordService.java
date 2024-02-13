package com.starter.kafka.service;

import com.starter.kafka.entity.FailureRecord;
import com.starter.kafka.repository.FailureRecordRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.stereotype.Service;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@Slf4j
@Service
public class FailureRecordService {

    FailureRecordRepository failureRecordRepository;


    public void save(ConsumerRecord<Integer, String> consumerRecord, Exception e, String status) {
        FailureRecord failureRecord = FailureRecord.builder()
                .id(null)
                .topic(consumerRecord.topic())
                .keyValue(consumerRecord.key())
                .errorRecord(consumerRecord.value())
                .partition(consumerRecord.partition())
                .offsetValue(consumerRecord.offset())
                .exception(e.getCause().getMessage())
                .status(status)
                .build();

        failureRecordRepository.save(failureRecord);
    }
}
