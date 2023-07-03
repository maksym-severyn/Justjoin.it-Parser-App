package com.example.justjoinparser.service;

import com.example.justjoinparser.dto.OfferDto;
import com.example.justjoinparser.filter.City;
import com.example.justjoinparser.filter.PositionLevel;
import com.example.justjoinparser.filter.Technology;
import reactor.core.publisher.Mono;

import java.util.Map;

public interface OfferService {

    Mono<OfferDto> save(OfferDto skill);

    Mono<Long> count();

    Mono<Map<String, Long>> findTopSkillsByParameters(Long top, PositionLevel positionLevel, City city,
                                                      Technology technology);

    Mono<Map<String, Long>> findSkillsByParameters(PositionLevel positionLevel, City city, Technology technology);
}
