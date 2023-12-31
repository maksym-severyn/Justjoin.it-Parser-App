package com.example.justjoinparser;

import com.example.justjoinparser.webdriver.WebdriverProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.reactive.config.EnableWebFlux;

@SpringBootApplication
@EnableWebFlux
@EnableScheduling
@EnableConfigurationProperties({WebdriverProperties.class})
public class JustJoinParserApplication {

    public static void main(String[] args) {
        SpringApplication.run(JustJoinParserApplication.class, args);
    }

}
