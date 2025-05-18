package com.getrosoft.com.getrosoftgenerateid.repository;

import com.getrosoft.com.getrosoftgenerateid.model.ProductTrackingId;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface TrackingIdRepository extends ReactiveMongoRepository<ProductTrackingId, String> {
}
