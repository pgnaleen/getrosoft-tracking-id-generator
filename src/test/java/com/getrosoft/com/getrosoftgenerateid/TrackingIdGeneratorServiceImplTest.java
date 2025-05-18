package com.getrosoft.com.getrosoftgenerateid;

import com.getrosoft.com.getrosoftgenerateid.dto.request.TrackingBaseRequest;
import com.getrosoft.com.getrosoftgenerateid.dto.request.TrackingIdGenerationRequestTracking;
import com.getrosoft.com.getrosoftgenerateid.exception.ProductTrackingIdGenerationException;
import com.getrosoft.com.getrosoftgenerateid.model.ProductTrackingId;
import com.getrosoft.com.getrosoftgenerateid.repository.TrackingIdRepository;
import com.getrosoft.com.getrosoftgenerateid.service.impl.TrackingIdGeneratorServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.data.redis.core.ReactiveValueOperations;
import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class TrackingIdGeneratorServiceImplTest {

    @Mock
    private ReactiveStringRedisTemplate redisTemplate;

    @Mock
    private ReactiveValueOperations<String, String> valueOperations;

    @Mock
    private ReactiveKafkaProducerTemplate<String, String> kafkaTemplate;

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
    void generateId_Success() {
        TrackingIdGenerationRequestTracking request = new TrackingIdGenerationRequestTracking("PRD-", "123", "Test Product", 9.99, "Category A") ;

        when(valueOperations.increment(any())).thenReturn(Mono.just(1L));
        when(trackingIdRepository.save(any(ProductTrackingId.class))).thenReturn(Mono.just(new ProductTrackingId("id", "123", "Test Product", "Category A", 9.99, "PRD-1")));
        when(kafkaTemplate.send(any(), (String) any())).thenReturn(Mono.empty());

        StepVerifier.create(service.generateId(request))
                .expectNextMatches(id -> id.contains("PRD-"))
                .verifyComplete();
    }

    @Test
    void generateId_InvalidRequestType() {
        TrackingBaseRequest invalidRequest = null;

        StepVerifier.create(service.generateId(invalidRequest))
                .expectErrorMatches(throwable -> throwable instanceof ProductTrackingIdGenerationException
                        && throwable.getMessage().contains("Invalid request type"))
                .verify();
    }

    @Test
    void generateId_Failure_Redis() {
        TrackingIdGenerationRequestTracking request = new TrackingIdGenerationRequestTracking("PRD-", "123", "Test Product", 9.99, "Category A");

        when(valueOperations.increment(any())).thenReturn(Mono.empty());

        StepVerifier.create(service.generateId(request))
                .expectErrorMatches(throwable -> throwable instanceof ProductTrackingIdGenerationException
                        && throwable.getMessage().contains("Unable to generate Id from redis"))
                .verify();
    }

    @Test
    void generateId_Failure_SaveToDatabase() {
        TrackingIdGenerationRequestTracking request = new TrackingIdGenerationRequestTracking("PRD-", "123", "Test Product", 9.99, "Category A");

        when(valueOperations.increment(any())).thenReturn(Mono.just(1L));
        when(trackingIdRepository.save(any(ProductTrackingId.class))).thenReturn(Mono.error(new RuntimeException("Database error")));

        StepVerifier.create(service.generateId(request))
                .expectErrorMatches(throwable -> throwable instanceof ProductTrackingIdGenerationException
                        && throwable.getMessage().contains("Failed to save tracking ID to database"))
                .verify();
    }

    @Test
    void generateId_Failure_PublishToKafka() {
        TrackingIdGenerationRequestTracking request = new TrackingIdGenerationRequestTracking("PRD-", "123", "Test Product", 9.99, "Category A");

        when(valueOperations.increment(any())).thenReturn(Mono.just(1L));
        when(trackingIdRepository.save(any(ProductTrackingId.class))).thenReturn(Mono.just(new ProductTrackingId("id", "123", "Test Product", "Category A", 9.99, "PRD-1")));
        when(kafkaTemplate.send(any(), (String)any())).thenReturn(Mono.error(new RuntimeException("Kafka error")));

        StepVerifier.create(service.generateId(request))
                .expectErrorMatches(throwable -> throwable instanceof ProductTrackingIdGenerationException
                        && throwable.getMessage().contains("Failed to publish tracking ID to Kafka"))
                .verify();
    }
}
