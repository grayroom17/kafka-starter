package com.starter.kafka.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.*;
import lombok.experimental.FieldDefaults;

@FieldDefaults(level = AccessLevel.PRIVATE)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
public class FailureRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer id;

    String topic;

    Integer keyValue;

    String errorRecord;

    Integer partition;

    Long offsetValue;

    String exception;

    String status;

}
