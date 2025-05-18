package com.getrosoft.com.getrosoftgenerateid.service;

import com.getrosoft.com.getrosoftgenerateid.dto.request.TrackingBaseRequest;

public interface TrackingIdGenerationService {
    String generateId(TrackingBaseRequest request);
}
