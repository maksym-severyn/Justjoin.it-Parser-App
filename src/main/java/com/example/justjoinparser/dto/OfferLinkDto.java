package com.example.justjoinparser.dto;

import com.example.justjoinparser.filter.City;
import com.example.justjoinparser.filter.PositionLevel;
import com.example.justjoinparser.filter.Technology;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@JsonIgnoreProperties(ignoreUnknown = true)
@Builder(toBuilder = true)
public record OfferLinkDto(
    @NotNull
    String link,
    @Schema(description = "offer seniority", example = "mid")
    @NotNull
    PositionLevel seniority,
    @Schema(description = "The technology to which the offer applies", example = "java")
    @NotNull
    City city,
    @Schema(description = "The technology to which the offer applies", example = "java")
    @NotNull
    Technology technology
) {
}
