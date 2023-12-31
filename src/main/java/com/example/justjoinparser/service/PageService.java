package com.example.justjoinparser.service;

import com.example.justjoinparser.dto.OfferDto;
import com.example.justjoinparser.dto.OfferLinkDto;
import reactor.core.publisher.Mono;

public interface PageService {

    Mono<OfferDto> parseOffers(OfferLinkDto offerLinkDto);
}
