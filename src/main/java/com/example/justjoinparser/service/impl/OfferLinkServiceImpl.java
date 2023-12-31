package com.example.justjoinparser.service.impl;

import com.example.justjoinparser.converter.OfferLinkDtoToOfferLinkConverter;
import com.example.justjoinparser.dto.OfferLinkDto;
import com.example.justjoinparser.filter.City;
import com.example.justjoinparser.filter.PositionLevel;
import com.example.justjoinparser.filter.Technology;
import com.example.justjoinparser.model.OfferLink;
import com.example.justjoinparser.repo.OfferLinkRepository;
import com.example.justjoinparser.service.OfferLinkService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
class OfferLinkServiceImpl implements OfferLinkService {

    private final OfferLinkRepository repository;
    private final OfferLinkDtoToOfferLinkConverter offerLinkDtoToOfferLinkConverter;

    @Override
    public Mono<OfferLinkDto> save(String offerLink, PositionLevel seniority, City city, Technology technology) {
        return repository.existsById(offerLink)
            .filter(exists -> !exists)
            .flatMap(bool -> repository.save(
                OfferLink.builder()
                    .link(offerLink)
                    .seniority(seniority)
                    .technology(technology)
                    .city(city)
                    .build())
                .flatMap(savedOffer -> Mono.just(OfferLinkDto.builder().link(savedOffer.getLink()).build())
                    .doOnNext(saved -> log.trace("Saved offerDto with link: {}", saved.link()))
                ))
            .switchIfEmpty(
                repository.findById(offerLink)
                    .flatMap(existingOffer -> {
                        existingOffer.setCity(city);
                        existingOffer.setSeniority(seniority);
                        existingOffer.setTechnology(technology);
                        return Mono.just(offerLinkDtoToOfferLinkConverter.convertFrom(existingOffer));
                    })
            );
    }
}
