package com.example.justjoinparser.schedule;

import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "scheduler.init-parse-offers")
@Getter
@Setter
public class ScheduleProperties {

    private List<String> cities;
    private List<String> technologies;
    private List<String> seniority;
}
