package com.example.justjoinparser.dto;

import lombok.Builder;

@Builder(toBuilder = true)
public record SkillDto(
    String name,
    String level
) {
}
