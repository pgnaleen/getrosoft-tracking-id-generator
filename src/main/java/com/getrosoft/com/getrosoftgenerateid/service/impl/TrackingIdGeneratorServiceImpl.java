package com.getrosoft.com.getrosoftgenerateid.service.impl;

import com.getrosoft.com.getrosoftgenerateid.dto.param.TrackingBaseQueryParams;
import com.getrosoft.com.getrosoftgenerateid.dto.param.TrackingIdGenerationQueryParams;
import com.getrosoft.com.getrosoftgenerateid.exception.ProductTrackingIdGenerationException;
import com.getrosoft.com.getrosoftgenerateid.model.ProductTrackingId;
import com.getrosoft.com.getrosoftgenerateid.repository.TrackingIdRepository;
import com.getrosoft.com.getrosoftgenerateid.service.TrackingIdGenerationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Slf4j
@Service
public class TrackingIdGeneratorServiceImpl implements TrackingIdGenerationService {

    private final ReactiveStringRedisTemplate redisTemplate;
    private final ReactiveKafkaProducerTemplate<String, String> kafkaTemplate;
    private final TrackingIdRepository trackingIdRepository;
    private static final String REDIS_TRACKING_ID_KEY = "product-tracking-id";
    private static final String KAFKA_TOPIC = "product-tracking-id";

    @Autowired
    public TrackingIdGeneratorServiceImpl(ReactiveStringRedisTemplate redisTemplate,
                                          ReactiveKafkaProducerTemplate<String, String> kafkaTemplate,
                                          TrackingIdRepository trackingIdRepository) {
        this.redisTemplate = redisTemplate;
        this.kafkaTemplate = kafkaTemplate;
        this.trackingIdRepository = trackingIdRepository;
    }

    /**
     * Generates a tracking ID for the given request.
     *
     * @param request The request containing the product details.
     * @return The generated tracking ID.
     * @throws IllegalArgumentException If the request type is invalid.
     * @throws ProductTrackingIdGenerationException If ID generation fails.
     */
    @Override
    public Mono<String> generateId(TrackingBaseQueryParams request) throws RuntimeException{

        // this is java 16+ pattern matching. it checks type of object and cast in one line
        if (!(request instanceof TrackingIdGenerationQueryParams trackingRequest)) {
            log.info("Invalid request type");
            return Mono.error(new ProductTrackingIdGenerationException("Invalid request type: Expected ProductTrackingIdGenerationException."));
        }

        // generate distributed tracking id with help of redis and prefix.
        // redis will store tracking ids into given disk storage offline
        // save all product information with tracking id to mongo db
        // publish all product information with tracking id to kafka
        return generateTrackingId(trackingRequest)
                .flatMap(trackingId -> saveTrackingIdToDatabase(trackingRequest, trackingId)
                .flatMap(savedTrackingId -> publishProductTrackingIdToKafka(savedTrackingId)
                .thenReturn(savedTrackingId.generateJson())));
//        return generateTrackingId(trackingRequest)
//                .flatMap(trackingId -> saveTrackingIdToDatabase(trackingRequest, trackingId)
//                .map(ProductTrackingId::generateJson));
    }

    /**
     * Generates a unique tracking ID using the specified prefix and Redis.
     *
     * @param request The request containing the prefix for the tracking ID.
     * @return The generated tracking ID.
     * @throws ProductTrackingIdGenerationException If ID generation fails in Redis.
     */
    private Mono<String> generateTrackingId(TrackingIdGenerationQueryParams request) throws RuntimeException {
        log.debug("Generating tracking id for {}", request);

        // redis will write tracking number to the disk offline
        return redisTemplate.opsForValue()
                .increment(REDIS_TRACKING_ID_KEY)
                .map(id -> request.getOriginCountryId() + Long.toString(id, 36).toUpperCase())
                .switchIfEmpty(Mono.error(new ProductTrackingIdGenerationException("Unable to generate Id from redis")));
    }

    /**
     * Publishes the generated tracking ID to the specified Kafka topic.
     *
     * @param productTrackingId The product details to be published.
     * @throws ProductTrackingIdGenerationException If Kafka publish fails.
     */
    private Mono<Void> publishProductTrackingIdToKafka(ProductTrackingId productTrackingId) {
        log.info("Publishing product tracking id to kafka {}", productTrackingId);

        return kafkaTemplate.send(KAFKA_TOPIC, productTrackingId.generateJson())
                .then()
                .onErrorResume(e -> Mono.error(
                                new ProductTrackingIdGenerationException("Failed to publish tracking ID to Kafka: "
                                        + e.getMessage())));
    }

    /**
     * Saves the generated tracking ID and product details to the database.
     *
     * @param queryParams The queryParams containing product details.
     * @param trackingId The generated tracking ID.
     * @throws ProductTrackingIdGenerationException If database save operation fails.
     */
    private Mono<ProductTrackingId> saveTrackingIdToDatabase(TrackingIdGenerationQueryParams queryParams, String trackingId) {
        log.debug("Saving tracking id to database");

        ProductTrackingId productTrackingId = new ProductTrackingId(null,
                queryParams.getOriginCountryId(),
                queryParams.getDestinationCountryId(),
                queryParams.getWeight(),
                queryParams.getCreatedAt(),
                UUID.fromString(queryParams.getCustomerId()),
                queryParams.getCustomerName(),
                queryParams.getCustomerSlug(),
                trackingId);

        return trackingIdRepository.save(productTrackingId)
                .onErrorMap(e -> new ProductTrackingIdGenerationException("Failed to save tracking ID to database: "
                        + e.getMessage()));
    }
}
