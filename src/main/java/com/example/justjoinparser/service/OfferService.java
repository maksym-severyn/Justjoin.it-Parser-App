package com.example.justjoinparser.service;

import com.example.justjoinparser.dto.OfferDto;
import com.example.justjoinparser.model.Skill;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface OfferService {

    Mono<OfferDto> save(OfferDto skill);

    Flux<Skill> findSkillsById(String offerId);
}
