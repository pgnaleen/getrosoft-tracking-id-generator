package com.getrosoft.com.getrosoftgenerateid.exception;

import java.time.LocalDateTime;

public class ProductTrackingIdGenerationExeption extends RuntimeException {

    private final LocalDateTime timestamp;
    private final String errorCode;

    public ProductTrackingIdGenerationExeption(String message) {
        super(message);
        this.timestamp = LocalDateTime.now();
        this.errorCode = "TRACKING_ID_GENERATION_ERROR";
    }

    @Override
    public String toString() {
        return "TrackingIdGenerationException{" +
                "timestamp=" + timestamp +
                ", errorCode='" + errorCode + '\'' +
                ", message='" + getMessage() + '\'' +
                '}';
    }
}
