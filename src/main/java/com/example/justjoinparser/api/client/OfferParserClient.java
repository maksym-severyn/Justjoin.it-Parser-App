package com.example.justjoinparser.api.client;

import com.example.justjoinparser.dto.OfferDto;
import com.example.justjoinparser.dto.OfferLinkDto;
import reactor.core.publisher.Mono;

public interface OfferParserClient {

    Mono<OfferDto> parseOffer(OfferLinkDto offerLink);
}
