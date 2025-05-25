package com.getrosoft.com.getrosoftgenerateid.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.util.UUID;

@Document(collection = "product_tracking_ids")
@CompoundIndexes({
        @CompoundIndex(name = "destination_country_id_customer_slug_idx", def = "{'destinationCountryId': 1, 'customerSlug': 1}", unique = true)
})
public record ProductTrackingId(
        @Id String id,

        String originCountryId,

        String destinationCountryId,

        BigDecimal weight,

        @Indexed
        String createdAt,

        @Indexed(unique = true)
        UUID customerId,

        String customerName,

        @Indexed
        String customerSlug,

        @Indexed(unique = true)
        String trackingId
) {
    public String generateJson() {
        return """
               {
                   "tracking_number": "%s",
                   "created_at": "%s"
               }
               """.formatted(trackingId, createdAt);
    }
}