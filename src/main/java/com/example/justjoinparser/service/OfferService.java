package com.example.justjoinparser.service;

import com.example.justjoinparser.dto.OfferDto;
import com.example.justjoinparser.filter.City;
import com.example.justjoinparser.filter.PositionLevel;
import com.example.justjoinparser.filter.Technology;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public interface OfferService {

    Flux<OfferDto> parseOffers(PositionLevel positionLevel, City city, Technology technology);
}
