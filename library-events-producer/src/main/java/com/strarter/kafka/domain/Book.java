package com.strarter.kafka.domain;

public record Book(
        Integer bookId,
        String bookName,
        String bookAuthor
) {
}
