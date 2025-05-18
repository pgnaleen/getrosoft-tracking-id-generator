package com.getrosoft.com.getrosoftgenerateid.controller;

import com.getrosoft.com.getrosoftgenerateid.dto.request.TrackingIdGenerationRequest;
import com.getrosoft.com.getrosoftgenerateid.service.TrackingIdGenerationService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/products/tracking/ids")
public class ProductTrackingIdGenerationController {
    private static final Logger logger = LoggerFactory.getLogger(ProductTrackingIdGenerationController.class);

    @Autowired
    private TrackingIdGenerationService trackingIdGenerationService;

    @PostMapping("/generate")
    public ResponseEntity<String> generateTrackingIds(@Valid @RequestBody TrackingIdGenerationRequest request) {
        logger.info("Received request {}", request);

        try {
            return ResponseEntity.ok(trackingIdGenerationService.generateId(request));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
