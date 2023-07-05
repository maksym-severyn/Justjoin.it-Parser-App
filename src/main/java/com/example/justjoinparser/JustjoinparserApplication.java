package com.example.justjoinparser;

import com.example.justjoinparser.schedule.ScheduleProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.reactive.config.EnableWebFlux;

@SpringBootApplication
@EnableWebFlux
@EnableScheduling
@EnableConfigurationProperties(ScheduleProperties.class)
public class JustjoinparserApplication {

    public static void main(String[] args) {
        SpringApplication.run(JustjoinparserApplication.class, args);
    }

}
