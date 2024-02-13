package com.starter.kafka.repository;

import com.starter.kafka.entity.FailureRecord;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FailureRecordRepository extends JpaRepository<FailureRecord, Integer> {


}
