package com.starter.kafka.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
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
    Integer bookId;

    String bookName;

    String bookAuthor;

    @JsonBackReference
    @OneToOne
    @JoinColumn(name = "libraryEventId")
    LibraryEvent libraryEvent;

}
