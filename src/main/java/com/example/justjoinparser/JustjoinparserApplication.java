package com.example.justjoinparser;

import com.example.justjoinparser.repo.OfferRepository;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@SpringBootApplication
@EnableMongoRepositories(basePackageClasses = OfferRepository.class)
@EnableWebMvc
@EnableScheduling
public class JustjoinparserApplication {

    public static void main(String[] args) {
        SpringApplication.run(JustjoinparserApplication.class, args);
    }

}
