package com.example.justjoinparser.service;

import com.example.justjoinparser.dto.OfferDto;
import com.example.justjoinparser.filter.City;
import com.example.justjoinparser.filter.PositionLevel;
import com.example.justjoinparser.filter.Technology;
import reactor.core.publisher.Flux;

public interface PageService {

    Flux<OfferDto> parseOffers(PositionLevel positionLevel, City city, Technology technology);
}
