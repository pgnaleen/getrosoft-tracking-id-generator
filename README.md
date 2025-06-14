## Setup Guide

### step 01:
#### run docker compose up inside mongodb, redis, kafka, prometheus, zipkin

### step 02:
#### configure those urls inside application.properties file if there is any deviation

### step 03:
#### start the spring boot application
to build the application
./gradlew bootJar

then
cd deployments

build docker image
docker build -t tracking-app .

run docker image
docker run -p 8080:8080 tracking-app

### step 04:
#### send below get request
http://localhost:8080/api/v1/products/next-tracking-number?origin_country_id=LK&destination_country_id=US&weight=0.1&created_at=2025-05-24T15%3A30%3A00.124%2B05%3A30&customer_id=de618594-b59b-425e-9db4-943979e1bd49&customer_name=anold%20shodinger&customer_slug=anold-shodinger

## Publicly hosted application in AWS Cloud Platform
URL: http://54.161.179.17:8080/api/v1/products/next-tracking-number?origin_country_id=LK&destination_country_id=US&weight=0.1&created_at=2025-05-24T15%3A30%3A00.124%2B05%3A30&customer_id=de618594-b59b-425e-9db4-943979e1bd49&customer_name=anold%20shodinger&customer_slug=anold-shodinger