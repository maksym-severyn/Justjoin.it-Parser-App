package com.example.justjoinparser.service;

import com.example.justjoinparser.dto.OfferDto;
import com.example.justjoinparser.filter.City;
import com.example.justjoinparser.filter.PositionLevel;
import com.example.justjoinparser.filter.Technology;
import java.util.Map;
import reactor.core.publisher.Mono;

public interface OfferService {

    Mono<OfferDto> save(OfferDto offerDtoToSave);

}
