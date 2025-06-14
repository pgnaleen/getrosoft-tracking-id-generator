package com.getrosoft.com.getrosoftgenerateid;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@SpringBootApplication
@EnableMongoRepositories
public class GetrosoftTrackingIdGeneratorApplication {

    public static void main(String[] args) {
        SpringApplication.run(GetrosoftTrackingIdGeneratorApplication.class, args);
    }
}
