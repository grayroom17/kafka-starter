package com.strarter.kafka.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.strarter.kafka.domain.LibraryEvent;
import com.strarter.kafka.producer.LibraryEventProducer;
import com.strarter.kafka.util.TestUtil;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.mockito.ArgumentMatchers.isA;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

//@ExtendWith(MockitoExtension.class)
@WebMvcTest(LibraryEventsController.class)
class LibraryEventsControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    LibraryEventProducer libraryEventProducer;


    @Test
    void sendEventAsync() throws Exception {
        Mockito.when(libraryEventProducer.sendLibraryEvent(isA(LibraryEvent.class)))
                .thenReturn(null);


        String value = objectMapper.writeValueAsString(TestUtil.libraryEventRecord());

        mockMvc.perform(MockMvcRequestBuilders.post("/v1/async-event")
                        .content(value)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());
    }

    @Test
    void sendEventAsyncWithProducerRecord() throws Exception {
        Mockito.when(libraryEventProducer.sendLibraryEvent(isA(LibraryEvent.class)))
                .thenReturn(null);


        String value = objectMapper.writeValueAsString(TestUtil.libraryEventRecord());

        mockMvc.perform(MockMvcRequestBuilders.post("/v1/async-event")
                        .content(value)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());
    }

    @Test
    void sendEventAsyncWithProducerRecordWithHeaders() throws Exception {
        Mockito.when(libraryEventProducer.sendLibraryEvent(isA(LibraryEvent.class)))
                .thenReturn(null);


        String value = objectMapper.writeValueAsString(TestUtil.libraryEventRecord());

        mockMvc.perform(MockMvcRequestBuilders.post("/v1/async-event")
                        .content(value)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());
    }

    @Test
    void sendEventSync() throws Exception {
        Mockito.when(libraryEventProducer.sendLibraryEvent(isA(LibraryEvent.class)))
                .thenReturn(null);


        String value = objectMapper.writeValueAsString(TestUtil.libraryEventRecord());

        mockMvc.perform(MockMvcRequestBuilders.post("/v1/async-event")
                        .content(value)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());
    }

}