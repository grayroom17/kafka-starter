package com.starter.kafka.scheduler;

import com.starter.kafka.entity.FailureRecord;
import com.starter.kafka.repository.FailureRecordRepository;
import com.starter.kafka.service.LibraryEventsService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@Slf4j
@Component
public class RetryScheduler {

    FailureRecordRepository failureRecordRepository;
    LibraryEventsService libraryEventsService;

    @Scheduled(fixedRate = 10000)
    public void retryFailedRecords() {
        log.info("Retrying Failed Records Started!");
        failureRecordRepository.findAllByStatus("RETRY")
                .forEach(failureRecord -> {
                    log.info("Retrying Failed Record : {} ", failureRecord);
                    ConsumerRecord<Integer, String> consumerRecord = buildConsumerRecord(failureRecord);
                    try {
                        libraryEventsService.processLibraryEvent(consumerRecord);
                        failureRecord.setStatus("SUCCESS");
                        failureRecordRepository.save(failureRecord);
                    } catch (Exception e) {
                        log.error("Exception in retryFailedRecords : {}", e.getMessage(), e);
                    }
                });
    }

    private ConsumerRecord<Integer, String> buildConsumerRecord(FailureRecord failureRecord) {
        return new ConsumerRecord<>(failureRecord.getTopic(),
                failureRecord.getPartition(),
                failureRecord.getOffsetValue(),
                failureRecord.getKeyValue(),
                failureRecord.getErrorRecord());
    }

}
