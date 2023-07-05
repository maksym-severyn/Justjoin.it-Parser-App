package com.example.justjoinparser.config;

import com.example.justjoinparser.converter.StringToCityEnumConverter;
import com.example.justjoinparser.converter.StringToRequestPositionLevelEnumConverter;
import com.example.justjoinparser.converter.StringToTechnologyEnumConverter;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.reactive.config.WebFluxConfigurer;

@Configuration
public class WebConfig implements WebFluxConfigurer {

    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverter(new StringToCityEnumConverter());
        registry.addConverter(new StringToTechnologyEnumConverter());
        registry.addConverter(new StringToRequestPositionLevelEnumConverter());
    }
}

