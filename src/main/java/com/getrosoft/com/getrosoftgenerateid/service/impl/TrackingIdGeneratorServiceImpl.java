package com.getrosoft.com.getrosoftgenerateid.service.impl;

import com.getrosoft.com.getrosoftgenerateid.dto.param.TrackingBaseQueryParams;
import com.getrosoft.com.getrosoftgenerateid.dto.param.TrackingIdGenerationQueryParams;
import com.getrosoft.com.getrosoftgenerateid.exception.ProductTrackingIdGenerationException;
import com.getrosoft.com.getrosoftgenerateid.model.ProductTrackingId;
import com.getrosoft.com.getrosoftgenerateid.repository.TrackingIdRepository;
import com.getrosoft.com.getrosoftgenerateid.service.TrackingIdGenerationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
public class TrackingIdGeneratorServiceImpl implements TrackingIdGenerationService {

    private final StringRedisTemplate redisTemplate;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final TrackingIdRepository trackingIdRepository;

    private static final String REDIS_TRACKING_ID_KEY = "product-tracking-id";
    private static final String KAFKA_TOPIC = "product-tracking-id";

    @Autowired
    public TrackingIdGeneratorServiceImpl(StringRedisTemplate redisTemplate,
                                          KafkaTemplate<String, String> kafkaTemplate,
                                          TrackingIdRepository trackingIdRepository) {
        this.redisTemplate = redisTemplate;
        this.kafkaTemplate = kafkaTemplate;
        this.trackingIdRepository = trackingIdRepository;
    }

    /**
     * Generates a tracking ID for the given request.
     *
     * @param request The request containing the product details.
     * @return The generated tracking ID JSON string.
     * @throws IllegalArgumentException If the request type is invalid.
     * @throws ProductTrackingIdGenerationException If ID generation fails.
     */
    @Override
    public String generateId(TrackingBaseQueryParams request) {
        if (!(request instanceof TrackingIdGenerationQueryParams trackingRequest)) {
            log.info("Invalid request type: expected TrackingIdGenerationQueryParams but received {}", request.getClass());
            throw new ProductTrackingIdGenerationException("Invalid request type: Expected TrackingIdGenerationQueryParams.");
        }

        try {
            // Generate distributed tracking id using Redis
            String trackingId = generateTrackingId(trackingRequest);

            // Save to MongoDB
            ProductTrackingId savedTrackingId = saveTrackingIdToDatabase(trackingRequest, trackingId);

            // Publish to Kafka
            publishProductTrackingIdToKafka(savedTrackingId);

            return savedTrackingId.generateJson();
        } catch (Exception e) {
            log.error("Failed to generate tracking ID", e);
            throw new ProductTrackingIdGenerationException("Failed to generate tracking ID: " + e.getMessage());
        }
    }

    /**
     * Generates a unique tracking ID using Redis INCR command and prefix.
     *
     * @param request The request containing the prefix.
     * @return The generated tracking ID string.
     * @throws ProductTrackingIdGenerationException If Redis operation fails.
     */
    private String generateTrackingId(TrackingIdGenerationQueryParams request) {
        log.debug("Generating tracking id for {}", request);

        Long incrementedValue = redisTemplate.opsForValue().increment(REDIS_TRACKING_ID_KEY);
        if (incrementedValue == null) {
            throw new ProductTrackingIdGenerationException("Unable to generate Id from Redis");
        }

        return request.getDestinationCountryId() + Long.toString(incrementedValue, 36).toUpperCase();
    }

    /**
     * Publishes the generated tracking ID JSON to Kafka topic.
     *
     * @param productTrackingId The product tracking details.
     * @throws ProductTrackingIdGenerationException If Kafka publishing fails.
     */
    private void publishProductTrackingIdToKafka(ProductTrackingId productTrackingId) {
        log.info("Publishing product tracking id to kafka: {}", productTrackingId);
        try {
            kafkaTemplate.send(KAFKA_TOPIC, productTrackingId.generateJson()).get();  // wait for send result synchronously
        } catch (Exception e) {
            throw new ProductTrackingIdGenerationException("Failed to publish tracking ID to Kafka: " + e.getMessage());
        }
    }

    /**
     * Saves the generated tracking ID and product details to the database.
     *
     * @param queryParams The query params containing product info.
     * @param trackingId The generated tracking ID string.
     * @return Saved ProductTrackingId entity.
     * @throws ProductTrackingIdGenerationException If save operation fails.
     */
    private ProductTrackingId saveTrackingIdToDatabase(TrackingIdGenerationQueryParams queryParams, String trackingId) {
        log.debug("Saving tracking id to database");

        ProductTrackingId productTrackingId = new ProductTrackingId(
                null,
                queryParams.getOriginCountryId(),
                queryParams.getDestinationCountryId(),
                queryParams.getWeight(),
                queryParams.getCreatedAt(),
                UUID.fromString(queryParams.getCustomerId()),
                queryParams.getCustomerName(),
                queryParams.getCustomerSlug(),
                trackingId);

        try {
            return trackingIdRepository.save(productTrackingId);
        } catch (Exception e) {
            throw new ProductTrackingIdGenerationException("Failed to save tracking ID to database: " + e.getMessage());
        }
    }
}
