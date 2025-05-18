package com.getrosoft.com.getrosoftgenerateid.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public non-sealed class TrackingIdGenerationRequestTracking implements TrackingBaseRequest {
    @NotBlank(message = "Prefix must not be blank")
    @Pattern(regexp = "^[A-Za-z]{1,5}$", message = "Prefix must contain only letters, max 5 characters")
    private String prefix;

    @NotBlank(message = "Product Id is required")
    private String productId;

    @NotBlank(message = "Product name is required")
    private String productName;

    @NotNull(message = "Product price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Product price must be positive")
    private Double productPrice;

    @NotBlank(message = "Product category is required")
    private String productCategory;
}
