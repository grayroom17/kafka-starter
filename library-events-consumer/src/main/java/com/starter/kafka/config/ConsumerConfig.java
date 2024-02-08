package com.starter.kafka.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.kafka.ConcurrentKafkaListenerContainerFactoryConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.dao.RecoverableDataAccessException;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.ExponentialBackOffWithMaxRetries;
import org.springframework.util.backoff.ExponentialBackOff;
import org.springframework.util.backoff.FixedBackOff;

import java.util.List;

@Slf4j
@Configuration
public class ConsumerConfig {

    public DefaultErrorHandler errorHandler() {
//        FixedBackOff fixedBackOff = new FixedBackOff(1000L, 2);
//        DefaultErrorHandler errorHandler = new DefaultErrorHandler(fixedBackOff);

        ExponentialBackOffWithMaxRetries exponentialBackOff = new ExponentialBackOffWithMaxRetries(5);
        exponentialBackOff.setInitialInterval(1000);
        exponentialBackOff.setMultiplier(2);
        exponentialBackOff.setMaxInterval(4000);
        DefaultErrorHandler errorHandler = new DefaultErrorHandler(exponentialBackOff);

        errorHandler.setRetryListeners((consumerRecord, ex, deliveryAttempt)
                -> log.info("Failed Record in Retry Listener, Exception : {} , deliveryAttempt : {} ",
                ex.getMessage(),
                deliveryAttempt));

//        List<Class<? extends Exception>> notRetryableExceptions = List.of(IllegalArgumentException.class);
//        notRetryableExceptions.forEach(errorHandler::addNotRetryableExceptions);

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