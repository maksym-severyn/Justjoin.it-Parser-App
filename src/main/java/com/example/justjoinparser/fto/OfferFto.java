package com.example.justjoinparser.fto;

import com.example.justjoinparser.dto.SkillDto;
import com.example.justjoinparser.filter.City;
import com.example.justjoinparser.filter.PositionLevel;
import com.example.justjoinparser.filter.Technology;
import java.util.List;
import lombok.Builder;

@Builder(toBuilder = true)
public record OfferFto(
    PositionLevel seniority,
    City city,
    Technology technology,
    String offerLink,
    List<SkillDto> skills
) {
}
