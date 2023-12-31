package com.example.justjoinparser.dto;

import lombok.Builder;

@Builder(toBuilder = true)
public record OfferLinkDto(String link) {
}
