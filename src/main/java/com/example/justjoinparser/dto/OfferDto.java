package com.example.justjoinparser.dto;

import com.example.justjoinparser.filter.City;
import com.example.justjoinparser.filter.PositionLevel;
import com.example.justjoinparser.filter.Technology;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;

@Builder(toBuilder = true)
public record OfferDto(
    String id,
    PositionLevel seniority,
    City city,
    Technology technology,
    String offerLink,
    List<SkillDto> skills,
    LocalDateTime createdDate,
    LocalDateTime lastModifiedDate
) {
}
