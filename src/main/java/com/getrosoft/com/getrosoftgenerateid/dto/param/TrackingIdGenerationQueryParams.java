package com.getrosoft.com.getrosoftgenerateid.dto.param;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.getrosoft.com.getrosoftgenerateid.utils.validator.ValidIsoCountryCode;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public non-sealed class TrackingIdGenerationQueryParams implements TrackingBaseQueryParams {
    @JsonProperty("origin_country_id")
    @ValidIsoCountryCode
    @NotBlank(message = "origin_country_id query param must not be blank")
    private String originCountryId;

    @JsonProperty("destination_country_id")
    @ValidIsoCountryCode
    @NotBlank(message = "destination_country_id query param must not be blank")
    private String destinationCountryId;

    @NotNull(message = "weight is required")
    @DecimalMin(value = "0.001", message = "weight must be greater than 0")
    @Digits(integer = 5, fraction = 3, message = "weight must have up to 3 decimal places")
    private BigDecimal weight;

    @Pattern(
            regexp = "^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}(\\.\\d{1,9})?(Z|[+-]\\d{2}:\\d{2})$",
            message = "created_at query param must be in RFC 3339 format"
    )
    @NotBlank(message = "created_at query param is required")
    private String createdAt;

    @NotNull(message = "customer_id query param is required")
    @Pattern(
            regexp = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[1-5][0-9a-fA-F]{3}-[89abAB][0-9a-fA-F]{3}-[0-9a-fA-F]{12}$",
            message = "please provide proper UUID for customer_id query param"
    )
    private String customerId;

    @NotBlank(message = "customer_name query param is required")
    @Size(max = 120, message = "max size allowed for customer_name is 120 characters")
    private String customerName;

    @Pattern(regexp = "^[a-z0-9]+(-[a-z0-9]+)*$", message = "customer_slug query param must be in kebab-case.")
    @Size(max = 130, message = "max size allowed for customer_slug is 130 characters")
    @NotBlank(message = "customer_slug query param is required")
    private String customerSlug;
}
