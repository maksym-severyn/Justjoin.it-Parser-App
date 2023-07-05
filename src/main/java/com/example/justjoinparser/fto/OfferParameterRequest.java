package com.example.justjoinparser.fto;

import com.example.justjoinparser.filter.City;
import com.example.justjoinparser.filter.PositionLevel;
import com.example.justjoinparser.filter.Technology;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import javax.validation.constraints.NotNull;
import lombok.Builder;

@JsonIgnoreProperties(ignoreUnknown = true)
@Builder
public record OfferParameterRequest(
        @NotNull
        City city,
        @NotNull
        Technology technology,
        @NotNull
        PositionLevel seniority
) {
}
