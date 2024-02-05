package com.starter.kafka.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@FieldDefaults(level = AccessLevel.PRIVATE)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
public class LibraryEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer libraryEventId;

    @Enumerated(EnumType.STRING)
    LibraryEventType libraryEventType;

    @ToString.Exclude
    @OneToOne(mappedBy = "libraryEvent",
            cascade = CascadeType.ALL)
    Book book;

}
