package com.getrosoft.com.getrosoftgenerateid.repository;

import com.getrosoft.com.getrosoftgenerateid.model.ProductTrackingId;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface TrackingIdRepository extends MongoRepository<ProductTrackingId, String> {
}
