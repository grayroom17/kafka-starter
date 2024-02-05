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
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer bookId;

    String bookName;

    String bookAuthor;

    @OneToOne
    @JoinColumn(name = "libraryEventId")
    LibraryEvent libraryEvent;

}
