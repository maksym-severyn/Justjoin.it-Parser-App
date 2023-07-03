package com.example.justjoinparser.fto;

import com.example.justjoinparser.filter.City;
import com.example.justjoinparser.filter.Technology;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;

@JsonIgnoreProperties(ignoreUnknown = true)
@Builder
public record OfferParameterRequest(
        City city,
        Technology technology,
        RequestPositionLevel seniority
) {
}
