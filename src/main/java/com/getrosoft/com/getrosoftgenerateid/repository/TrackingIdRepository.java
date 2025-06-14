package com.getrosoft.com.getrosoftgenerateid.repository;

import com.getrosoft.com.getrosoftgenerateid.model.ProductTrackingId;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

// reactive mongo repository have been used for non-blocking style coding
public interface TrackingIdRepository extends ReactiveMongoRepository<ProductTrackingId, String> {
}
