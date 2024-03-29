package com.starter.kafka.repository;

import com.starter.kafka.entity.FailureRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FailureRecordRepository extends JpaRepository<FailureRecord, Integer> {

    List<FailureRecord> findAllByStatus(String retry);

}
