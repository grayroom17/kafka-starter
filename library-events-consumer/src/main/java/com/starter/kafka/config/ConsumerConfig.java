package com.starter.kafka.config;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.TopicPartition;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.kafka.ConcurrentKafkaListenerContainerFactoryConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.dao.RecoverableDataAccessException;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.ExponentialBackOffWithMaxRetries;

import java.util.List;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@Slf4j
@Configuration
public class ConsumerConfig {

    KafkaTemplate<Integer, String> kafkaTemplate;

    @NonFinal
    @Value("${kafka.topics.retry}")
    String retryTopic;

    @NonFinal
    @Value("${kafka.topics.dlt}")
    String deadLetterTopic;

    public DeadLetterPublishingRecoverer publishingRecoverer() {
        return new DeadLetterPublishingRecoverer(kafkaTemplate,
                (r, e) -> {
                    log.error("Exception in publishingRecoverer : {}", e.getMessage(), e);
                    if (e.getCause() instanceof RecoverableDataAccessException) {
                        return new TopicPartition(retryTopic, r.partition());
                    } else {
                        return new TopicPartition(deadLetterTopic, r.partition());
                    }
                });
    }

    public DefaultErrorHandler errorHandler() {
//        FixedBackOff fixedBackOff = new FixedBackOff(1000L, 2);
//        DefaultErrorHandler errorHandler = new DefaultErrorHandler(fixedBackOff);

        ExponentialBackOffWithMaxRetries exponentialBackOff = new ExponentialBackOffWithMaxRetries(5);
        exponentialBackOff.setInitialInterval(1000);
        exponentialBackOff.setMultiplier(2);
        exponentialBackOff.setMaxInterval(4000);
        DefaultErrorHandler errorHandler = new DefaultErrorHandler(
                publishingRecoverer(),
                exponentialBackOff);

        errorHandler.setRetryListeners((consumerRecord, ex, deliveryAttempt)
                -> log.info("Failed Record in Retry Listener, Exception : {} , deliveryAttempt : {} ",
                ex.getMessage(),
                deliveryAttempt));

        List<Class<? extends Exception>> notRetryableExceptions = List.of(IllegalArgumentException.class);
        notRetryableExceptions.forEach(errorHandler::addNotRetryableExceptions);

        List<Class<? extends Exception>> retryableExceptions = List.of(RecoverableDataAccessException.class);
        retryableExceptions.forEach(errorHandler::addRetryableExceptions);

        return errorHandler;
    }

    @Bean
    ConcurrentKafkaListenerContainerFactory<?, ?> kafkaListenerContainerFactory(
            ConcurrentKafkaListenerContainerFactoryConfigurer configurer,
            ConsumerFactory<Object, Object> kafkaConsumerFactory) {
        ConcurrentKafkaListenerContainerFactory<Object, Object> factory = new ConcurrentKafkaListenerContainerFactory<>();
        configurer.configure(factory, kafkaConsumerFactory);
        factory.setConcurrency(3);
        factory.setCommonErrorHandler(errorHandler());
//        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL);
        return factory;
    }

}