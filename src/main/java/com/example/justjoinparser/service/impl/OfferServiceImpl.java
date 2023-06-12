package com.example.justjoinparser.service.impl;

import com.example.justjoinparser.converter.OfferDtoToOfferConverter;
import com.example.justjoinparser.dto.OfferDto;
import com.example.justjoinparser.model.Skill;
import com.example.justjoinparser.repo.OfferRepository;
import com.example.justjoinparser.service.OfferService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
class OfferServiceImpl implements OfferService {

    private final OfferDtoToOfferConverter converter;
    private final OfferRepository repository;

    @Override
    public Mono<OfferDto> save(OfferDto offerDtoToSave) {
        return repository.save(converter.convertTo(offerDtoToSave))
            .flatMap(savedOffer -> Mono.just(converter.convertFrom(savedOffer))
                .doOnNext(saved -> log.trace("Saved offerDto with id: {}", saved.id())));
    }

    @Override
    public Flux<Skill> findSkillsById(String offerId) {
        return repository.findSkillsById(offerId);
    }
}
