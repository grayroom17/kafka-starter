package com.starter.kafka.repository;

import com.starter.kafka.entity.LibraryEvent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LibraryEventsRepository extends JpaRepository<LibraryEvent, Integer> {



}
