package com.getrosoft.com.getrosoftgenerateid;

import com.getrosoft.com.getrosoftgenerateid.dto.param.TrackingBaseQueryParams;
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
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.data.redis.core.ReactiveValueOperations;
import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.util.UUID;

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
        TrackingIdGenerationQueryParams request = new TrackingIdGenerationQueryParams("LK", "US", new BigDecimal("1.123"), "2025-05-24T15:30:00.124+05:30" ,"de618594-b59b-425e-9db4-943979e1bd49", "anold shodinger","anold-shodinger") ;

        when(valueOperations.increment(any())).thenReturn(Mono.just(1L));
        when(trackingIdRepository.save(any(ProductTrackingId.class))).thenReturn(Mono.just(new ProductTrackingId("id", "LK", "US", new BigDecimal("1.123"), "2025-05-24T15:30:00.124+05:30" , UUID.fromString("de618594-b59b-425e-9db4-943979e1bd49"), "anold shodinger","anold-shodinger", "LK")));
        when(kafkaTemplate.send(any(), (String) any())).thenReturn(Mono.empty());

        StepVerifier.create(service.generateId(request))
                .expectNextMatches(id -> id.contains("LK"))
                .verifyComplete();
    }

    @Test
    void generateId_InvalidRequestType() {
        TrackingBaseQueryParams invalidRequest = null;

        StepVerifier.create(service.generateId(invalidRequest))
                .expectErrorMatches(throwable -> throwable instanceof ProductTrackingIdGenerationException
                        && throwable.getMessage().contains("Invalid request type"))
                .verify();
    }

    @Test
    void generateId_Failure_Redis() {
        TrackingIdGenerationQueryParams request = new TrackingIdGenerationQueryParams("LK", "US", new BigDecimal("1.123"), "2025-05-24T15:30:00.124+05:30" ,"de618594-b59b-425e-9db4-943979e1bd49", "anold shodinger","anold-shodinger");

        when(valueOperations.increment(any())).thenReturn(Mono.empty());

        StepVerifier.create(service.generateId(request))
                .expectErrorMatches(throwable -> throwable instanceof ProductTrackingIdGenerationException
                        && throwable.getMessage().contains("Unable to generate Id from redis"))
                .verify();
    }

    @Test
    void generateId_Failure_SaveToDatabase() {
        TrackingIdGenerationQueryParams request = new TrackingIdGenerationQueryParams("LK", "US", new BigDecimal("1.123"), "2025-05-24T15:30:00.124+05:30" ,"de618594-b59b-425e-9db4-943979e1bd49", "anold shodinger","anold-shodinger");

        when(valueOperations.increment(any())).thenReturn(Mono.just(1L));
        when(trackingIdRepository.save(any(ProductTrackingId.class))).thenReturn(Mono.error(new RuntimeException("Database error")));

        StepVerifier.create(service.generateId(request))
                .expectErrorMatches(throwable -> throwable instanceof ProductTrackingIdGenerationException
                        && throwable.getMessage().contains("Failed to save tracking ID to database"))
                .verify();
    }

    @Test
    void generateId_Failure_PublishToKafka() {
        TrackingIdGenerationQueryParams request = new TrackingIdGenerationQueryParams("LK", "US", new BigDecimal("1.123"), "2025-05-24T15:30:00.124+05:30" ,"de618594-b59b-425e-9db4-943979e1bd49", "anold shodinger","anold-shodinger");

        when(valueOperations.increment(any())).thenReturn(Mono.just(1L));
        when(trackingIdRepository.save(any(ProductTrackingId.class))).thenReturn(Mono.just(new ProductTrackingId("id", "LK", "US", new BigDecimal("1.123"), "2025-05-24T15:30:00.124+05:30" , UUID.fromString("de618594-b59b-425e-9db4-943979e1bd49"), "anold shodinger","anold-shodinger", "LK")));
        when(kafkaTemplate.send(any(), (String)any())).thenReturn(Mono.error(new RuntimeException("Kafka error")));

        StepVerifier.create(service.generateId(request))
                .expectErrorMatches(throwable -> throwable instanceof ProductTrackingIdGenerationException
                        && throwable.getMessage().contains("Failed to publish tracking ID to Kafka"))
                .verify();
    }
}
