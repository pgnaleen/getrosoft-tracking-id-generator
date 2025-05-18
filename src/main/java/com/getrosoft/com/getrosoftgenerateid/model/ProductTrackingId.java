package com.getrosoft.com.getrosoftgenerateid.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.getrosoft.com.getrosoftgenerateid.exception.ProductTrackingIdGenerationException;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@Document(collection = "product_tracking_id")
@CompoundIndexes({
        @CompoundIndex(name = "category_price_idx", def = "{'productCategory': 1, 'productName': 1}")
})
@AllArgsConstructor
@NoArgsConstructor
public class ProductTrackingId {
    @Id
    private String id;

    @Indexed(unique = true)
    private String productId;

    @Indexed
    private String productName;

    @Indexed
    private String productCategory;
    private double productPrice;

    @Indexed(unique = true)
    private String trackingId;

    public String generateJson() {
        ObjectMapper objectMapper = new ObjectMapper();

        try {
            return objectMapper.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            throw new ProductTrackingIdGenerationException("Error generating JSON for ProductTrackingId");
        }
    }
}
