package com.strarter.kafka.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicsConfig {

    @Value("${kafka.topic.library}")
    String eventName;

    @Bean
    public NewTopic libraryEvents() {
        return TopicBuilder
                .name(eventName)
                .partitions(3)
                .replicas(3)
                .build();
    }

}
