package com.getrosoft.com.getrosoftgenerateid.service.impl;

import com.getrosoft.com.getrosoftgenerateid.dto.request.BaseRequest;
import com.getrosoft.com.getrosoftgenerateid.dto.request.TrackingIdGenerationRequest;
import com.getrosoft.com.getrosoftgenerateid.exception.ProductTrackingIdGenerationException;
import com.getrosoft.com.getrosoftgenerateid.model.ProductTrackingId;
import com.getrosoft.com.getrosoftgenerateid.repository.TrackingIdRepository;
import com.getrosoft.com.getrosoftgenerateid.service.TrackingIdGenerationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.Optional;

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
     * @return The generated tracking ID.
     * @throws IllegalArgumentException If the request type is invalid.
     * @throws ProductTrackingIdGenerationException If ID generation fails.
     */
    @Override
    public String generateId(BaseRequest request) throws RuntimeException{

        if (!(request instanceof TrackingIdGenerationRequest))
            throw new ProductTrackingIdGenerationException("Invalid request type: Expected ProductTrackingIdGenerationException.");

        // generate distributed tracking id with help of redis and prefix.
        // redis will store tracking ids into given disk storage offline
        String trackingId = generateTrackingId((TrackingIdGenerationRequest)request);

        // publish all product information with tracking id to kafka
        ProductTrackingId productTrackingId = saveTrackingIdToDatabase((TrackingIdGenerationRequest)request, trackingId);

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
    private String generateTrackingId(TrackingIdGenerationRequest request) throws RuntimeException {

        // redis will write tracking number to the disk offline
        Long id = Optional.ofNullable(redisTemplate.opsForValue().increment(REDIS_TRACKING_ID_KEY, 1))
                .orElseThrow(() -> new ProductTrackingIdGenerationException("Unable to generate Id from redis"));

        return request.getPrefix() + id;
    }

    /**
     * Publishes the generated tracking ID to the specified Kafka topic.
     *
     * @param productTrackingId The product details to be published.
     * @throws ProductTrackingIdGenerationException If Kafka publish fails.
     */
    private void publishProductTrackingIdToKafka(ProductTrackingId productTrackingId) {

        try {
            kafkaTemplate.send(KAFKA_TOPIC, productTrackingId.generateJson());
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
    private ProductTrackingId saveTrackingIdToDatabase(TrackingIdGenerationRequest request, String trackingId) {
        ProductTrackingId productTrackingId = new ProductTrackingId();
        productTrackingId.setProductId(request.getProductId());
        productTrackingId.setProductName(request.getProductName());
        productTrackingId.setProductCategory(request.getProductCategory());
        productTrackingId.setProductPrice(request.getProductPrice());
        productTrackingId.setTrackingId(trackingId);

        try {
            productTrackingId = trackingIdRepository.save(productTrackingId);
        } catch (Exception e) {
            throw new ProductTrackingIdGenerationException("Failed to save tracking ID to database: " + e.getMessage());
        }

        return productTrackingId;
    }
}
