package com.getrosoft.com.getrosoftgenerateid;

import com.getrosoft.com.getrosoftgenerateid.dto.param.TrackingIdGenerationQueryParams;
import com.getrosoft.com.getrosoftgenerateid.exception.ProductTrackingIdGenerationException;
import com.getrosoft.com.getrosoftgenerateid.model.ProductTrackingId;
import com.getrosoft.com.getrosoftgenerateid.repository.TrackingIdRepository;
import com.getrosoft.com.getrosoftgenerateid.service.impl.TrackingIdGeneratorServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.kafka.core.KafkaTemplate;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class TrackingIdGeneratorServiceImplTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @Mock
    private KafkaTemplate<String, String> kafkaTemplate;

    @Mock
    private TrackingIdRepository trackingIdRepository;

    @InjectMocks
    private TrackingIdGeneratorServiceImpl service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    void generateId_Success() throws Exception {
        TrackingIdGenerationQueryParams request = new TrackingIdGenerationQueryParams(
                "LK", "US", new BigDecimal("1.123"), "2025-05-24T15:30:00.124+05:30",
                "de618594-b59b-425e-9db4-943979e1bd49", "anold shodinger", "anold-shodinger"
        );

        when(valueOperations.increment(any())).thenReturn(1L);
        when(trackingIdRepository.save(any(ProductTrackingId.class)))
                .thenReturn(new ProductTrackingId(
                        "id", "LK", "US", new BigDecimal("1.123"), "2025-05-24T15:30:00.124+05:30",
                        UUID.fromString("de618594-b59b-425e-9db4-943979e1bd49"),
                        "anold shodinger", "anold-shodinger", "LK1"
                ));
        // KafkaTemplate.send returns a ListenableFuture, mock to return completed future
        when(kafkaTemplate.send(any(), any())).thenReturn(CompletableFuture.completedFuture(null));

        String result = service.generateId(request);

        assertNotNull(result);
        assertTrue(result.contains("LK"));
        verify(valueOperations).increment(any());
        verify(trackingIdRepository).save(any());
        verify(kafkaTemplate).send(any(), any());
    }

    @Test
    void generateId_Failure_Redis() {
        TrackingIdGenerationQueryParams request = new TrackingIdGenerationQueryParams(
                "LK", "US", new BigDecimal("1.123"), "2025-05-24T15:30:00.124+05:30",
                "de618594-b59b-425e-9db4-943979e1bd49", "anold shodinger", "anold-shodinger"
        );

        when(valueOperations.increment(any())).thenReturn(null); // simulate Redis failure

        ProductTrackingIdGenerationException ex = assertThrows(ProductTrackingIdGenerationException.class,
                () -> service.generateId(request));

        assertTrue(ex.getMessage().contains("Unable to generate Id from Redis"));
    }

    @Test
    void generateId_Failure_SaveToDatabase() {
        TrackingIdGenerationQueryParams request = new TrackingIdGenerationQueryParams(
                "LK", "US", new BigDecimal("1.123"), "2025-05-24T15:30:00.124+05:30",
                "de618594-b59b-425e-9db4-943979e1bd49", "anold shodinger", "anold-shodinger"
        );

        when(valueOperations.increment(any())).thenReturn(1L);
        when(trackingIdRepository.save(any(ProductTrackingId.class))).thenThrow(new RuntimeException("Database error"));

        ProductTrackingIdGenerationException ex = assertThrows(ProductTrackingIdGenerationException.class,
                () -> service.generateId(request));

        assertTrue(ex.getMessage().contains("Failed to save tracking ID to database"));
    }

    @Test
    void generateId_Failure_PublishToKafka() {
        TrackingIdGenerationQueryParams request = new TrackingIdGenerationQueryParams(
                "LK", "US", new BigDecimal("1.123"), "2025-05-24T15:30:00.124+05:30",
                "de618594-b59b-425e-9db4-943979e1bd49", "anold shodinger", "anold-shodinger"
        );

        when(valueOperations.increment(any())).thenReturn(1L);
        when(trackingIdRepository.save(any(ProductTrackingId.class))).thenReturn(new ProductTrackingId(
                "id", "LK", "US", new BigDecimal("1.123"), "2025-05-24T15:30:00.124+05:30",
                UUID.fromString("de618594-b59b-425e-9db4-943979e1bd49"),
                "anold shodinger", "anold-shodinger", "LK1"
        ));
        when(kafkaTemplate.send(any(), any())).thenThrow(new RuntimeException("Kafka error"));

        ProductTrackingIdGenerationException ex = assertThrows(ProductTrackingIdGenerationException.class,
                () -> service.generateId(request));

        assertTrue(ex.getMessage().contains("Failed to publish tracking ID to Kafka"));
    }
}
