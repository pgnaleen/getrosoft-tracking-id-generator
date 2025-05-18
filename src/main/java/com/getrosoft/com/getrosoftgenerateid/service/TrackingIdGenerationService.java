package com.getrosoft.com.getrosoftgenerateid.service;

import com.getrosoft.com.getrosoftgenerateid.dto.request.BaseRequest;

public interface TrackingIdGenerationService {
    String generateId(BaseRequest request);
}
