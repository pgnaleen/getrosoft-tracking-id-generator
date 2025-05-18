package com.getrosoft.com.getrosoftgenerateid.service.impl;

import com.getrosoft.com.getrosoftgenerateid.dto.request.TrackingBaseRequest;
import com.getrosoft.com.getrosoftgenerateid.dto.request.TrackingIdGenerationRequestTracking;
import com.getrosoft.com.getrosoftgenerateid.exception.ProductTrackingIdGenerationException;
import com.getrosoft.com.getrosoftgenerateid.model.ProductTrackingId;
import com.getrosoft.com.getrosoftgenerateid.repository.TrackingIdRepository;
import com.getrosoft.com.getrosoftgenerateid.service.TrackingIdGenerationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate;
import org.springframework.stereotype.Service;

import java.util.Optional;

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
    public String generateId(TrackingBaseRequest request) throws RuntimeException{

        // this is java 16+ pattern matching. it checks type of object and cast in one line
        if (!(request instanceof TrackingIdGenerationRequestTracking trackingRequest))
            throw new ProductTrackingIdGenerationException("Invalid request type: Expected ProductTrackingIdGenerationException.");

        // generate distributed tracking id with help of redis and prefix.
        // redis will store tracking ids into given disk storage offline
        String trackingId = generateTrackingId(trackingRequest);

        // publish all product information with tracking id to kafka
        ProductTrackingId productTrackingId = saveTrackingIdToDatabase(trackingRequest, trackingId);

        // save all product information with tracking id to mongo db
        publishProductTrackingIdToKafka(productTrackingId);

        return productTrackingId.generateJson();
    }

    /**
     * Generates a unique tracking ID using the specified prefix and Redis.
     *
     * @param request The request containing the prefix for the tracking ID.
     * @return The generated tracking ID.
     * @throws ProductTrackingIdGenerationException If ID generation fails in Redis.
     */
    private String generateTrackingId(TrackingIdGenerationRequestTracking request) throws RuntimeException {

        // redis will write tracking number to the disk offline
        return Optional.of(redisTemplate.opsForValue().increment(REDIS_TRACKING_ID_KEY, 1).subscribe())
                .map(id -> request.getPrefix() + id)
                .orElseThrow(() -> new ProductTrackingIdGenerationException("Unable to generate Id from redis"));
    }

    /**
     * Publishes the generated tracking ID to the specified Kafka topic.
     *
     * @param productTrackingId The product details to be published.
     * @throws ProductTrackingIdGenerationException If Kafka publish fails.
     */
    private void publishProductTrackingIdToKafka(ProductTrackingId productTrackingId) {

        try {
            kafkaTemplate.send(KAFKA_TOPIC, productTrackingId.generateJson()).subscribe();
        } catch (Exception e) {
            throw new ProductTrackingIdGenerationException("Failed to publish tracking ID to Kafka: ");
        }
    }

    /**
     * Saves the generated tracking ID and product details to the database.
     *
     * @param request The request containing product details.
     * @param trackingId The generated tracking ID.
     * @throws ProductTrackingIdGenerationException If database save operation fails.
     */
    private ProductTrackingId saveTrackingIdToDatabase(TrackingIdGenerationRequestTracking request, String trackingId) {

        return Optional.of(request)
                .map(req -> new ProductTrackingId(null,  req.getProductId(), req.getProductName(),
                        req.getProductCategory(), req.getProductPrice(), trackingId))
                .map(trackingIdRepository::save)
                .orElseThrow(() -> new ProductTrackingIdGenerationException("Failed to save tracking ID to database: "));
    }
}
