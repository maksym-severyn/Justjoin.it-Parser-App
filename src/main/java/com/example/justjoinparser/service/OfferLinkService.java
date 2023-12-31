package com.example.justjoinparser.service;

import com.example.justjoinparser.dto.OfferLinkDto;
import reactor.core.publisher.Mono;

public interface OfferLinkService {

    Mono<OfferLinkDto> save(String offerLink);
}
