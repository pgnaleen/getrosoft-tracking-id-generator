package com.getrosoft.com.getrosoftgenerateid.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "product_tracking_ids")
@CompoundIndexes({
        @CompoundIndex(name = "product_id_category_idx", def = "{'productId': 1, 'productCategory': 1}", unique = true)
})
public record ProductTrackingId(
        @Id String id,

        @Indexed(unique = true)
        String productId,

        @Indexed
        String productName,

        @Indexed
        String productCategory,

        double productPrice,

        @Indexed(unique = true)
        String trackingId
) {
    public String generateJson() {
        return """
               {
                   "productId": "%s",
                   "productName": "%s",
                   "productCategory": "%s",
                   "productPrice": %.2f,
                   "trackingId": "%s"
               }
               """.formatted(productId, productName, productCategory, productPrice, trackingId);
    }
}