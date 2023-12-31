package com.example.justjoinparser.service.impl;

import com.example.justjoinparser.dto.OfferLinkDto;
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

    @Override
    public Mono<OfferLinkDto> save(String offerLink) {
        return repository.existsById(offerLink)
            .filter(exists -> !exists)
            .flatMap(bool -> repository.save(OfferLink.builder().link(offerLink).build())
                .flatMap(savedOffer -> Mono.just(OfferLinkDto.builder().link(savedOffer.getLink()).build())
                    .doOnNext(saved -> log.trace("Saved offerDto with link: {}", saved.link()))
                ))
            .switchIfEmpty(
                repository.findById(offerLink)
                    .flatMap(existingOffer -> Mono.just(OfferLinkDto.builder().link(existingOffer.getLink()).build()))
            );
    }
}
