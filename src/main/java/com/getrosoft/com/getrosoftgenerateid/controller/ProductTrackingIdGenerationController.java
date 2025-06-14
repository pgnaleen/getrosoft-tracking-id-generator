package com.getrosoft.com.getrosoftgenerateid.controller;

import com.getrosoft.com.getrosoftgenerateid.dto.param.TrackingIdGenerationQueryParams;
import com.getrosoft.com.getrosoftgenerateid.service.TrackingIdGenerationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
@Validated // this support group validations
@RequestMapping("/products")
public class ProductTrackingIdGenerationController {

    @Autowired
    private TrackingIdGenerationService trackingIdGenerationService;

    @GetMapping("/next-tracking-number")
    public Mono<String> generateTrackingIds(@Validated @ModelAttribute TrackingIdGenerationQueryParams queryParams) {
        log.info("Received request {}", queryParams);

        try {
            return trackingIdGenerationService.generateId(queryParams);
        } catch (Exception e) {
            return Mono.empty();
        }
    }
}
