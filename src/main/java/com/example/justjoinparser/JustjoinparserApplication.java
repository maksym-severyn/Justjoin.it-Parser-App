package com.example.justjoinparser;

import com.example.justjoinparser.repo.OfferRepository;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@SpringBootApplication
@EnableMongoRepositories(basePackageClasses = OfferRepository.class)
public class JustjoinparserApplication {

    public static void main(String[] args) {
        SpringApplication.run(JustjoinparserApplication.class, args);
    }

}
