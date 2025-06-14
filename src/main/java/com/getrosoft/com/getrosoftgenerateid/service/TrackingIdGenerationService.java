package com.getrosoft.com.getrosoftgenerateid.service;

import com.getrosoft.com.getrosoftgenerateid.dto.param.TrackingBaseQueryParams;

public interface TrackingIdGenerationService {
    String generateId(TrackingBaseQueryParams request);
}
