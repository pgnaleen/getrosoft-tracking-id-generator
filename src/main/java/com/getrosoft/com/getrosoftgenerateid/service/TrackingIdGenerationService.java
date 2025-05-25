package com.getrosoft.com.getrosoftgenerateid.service;

import com.getrosoft.com.getrosoftgenerateid.dto.param.TrackingBaseQueryParams;
import reactor.core.publisher.Mono;

public interface TrackingIdGenerationService {
    Mono<String> generateId(TrackingBaseQueryParams request);
}
