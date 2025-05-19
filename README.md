## Setup Guide

### step 01:
#### run docker compose up inside mongodb, redis, kafka, prometheus, zipkin

### step 02:
#### configure those urls inside application.properties file if there is any deviation

### step 03:
#### start the spring boot application

### step 04:
#### send below post request
URL: http://localhost:8080/api/v1/products/tracking/ids/generate
Headers: Content-Type: application/json
Body:
{
"productId": "12345",
"productName": "Sample Product",
"productCategory": "Electronics",
"productPrice": 99.99,
"trackingId": "test6072608"
}