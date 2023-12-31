package com.example.justjoinparser.service;

import com.example.justjoinparser.dto.OfferLinkDto;
import com.example.justjoinparser.filter.City;
import com.example.justjoinparser.filter.PositionLevel;
import com.example.justjoinparser.filter.Technology;
import reactor.core.publisher.Mono;

public interface OfferLinkService {

    Mono<OfferLinkDto> save(String offerLink, PositionLevel seniority, City city, Technology technology);
}
