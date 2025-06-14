package com.getrosoft.com.getrosoftgenerateid.controller;

import com.getrosoft.com.getrosoftgenerateid.dto.param.TrackingIdGenerationQueryParams;
import com.getrosoft.com.getrosoftgenerateid.service.TrackingIdGenerationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@Validated // this support group validations
@RequestMapping("/products")
public class ProductTrackingIdGenerationController {

    private final TrackingIdGenerationService trackingIdGenerationService;

    @Autowired
    public ProductTrackingIdGenerationController(TrackingIdGenerationService trackingIdGenerationService) {
        this.trackingIdGenerationService = trackingIdGenerationService;
    }

    @GetMapping("/next-tracking-number")
    public ResponseEntity<String> generateTrackingId(@Validated @ModelAttribute TrackingIdGenerationQueryParams queryParams) {
        log.info("Received tracking ID generation request: {}", queryParams);
        try {
            String trackingId = trackingIdGenerationService.generateId(queryParams);
            return ResponseEntity.ok(trackingId);
        } catch (Exception ex) {
            log.error("Failed to generate tracking ID for request {}: {}", queryParams, ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An error occurred while generating the tracking ID.");
        }
    }
}
