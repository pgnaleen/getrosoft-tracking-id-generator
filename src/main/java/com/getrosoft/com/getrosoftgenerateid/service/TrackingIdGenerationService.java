package com.getrosoft.com.getrosoftgenerateid.service;

import com.getrosoft.com.getrosoftgenerateid.dto.request.TrackingBaseRequest;
import reactor.core.publisher.Mono;

public interface TrackingIdGenerationService {
    Mono<String> generateId(TrackingBaseRequest request);
}
