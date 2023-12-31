package com.example.justjoinparser.dto;

import com.example.justjoinparser.filter.City;
import com.example.justjoinparser.filter.PositionLevel;
import com.example.justjoinparser.filter.Technology;
import lombok.Builder;

@Builder(toBuilder = true)
public record OfferLinkDto(
    String link,
    PositionLevel seniority,
    City city,
    Technology technology
) {
}
