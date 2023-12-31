package com.example.justjoinparser.controller;

import com.example.justjoinparser.dto.OfferDto;
import com.example.justjoinparser.dto.OfferLinkDto;
import com.example.justjoinparser.service.OfferService;
import com.example.justjoinparser.service.PageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/offer")
public class JustjoinitOfferController {

    private final PageService pageService;
    private final OfferService offerService;

    @PostMapping(value = "/parse")
    public Mono<OfferDto> initializeOfferParsingWithProvidedParams(
        @RequestBody OfferLinkDto request) {
        log.info("Parsing: {}", request.link());

        return
            pageService.parseOffers(request)
                .flatMap(offerService::save)
                .doOnNext(offerDto -> log.info("Parsed offer: {}", offerDto.id()))
//                .map(offerDtoToOfferFtoConverter::convertTo)
        ;
    }
}
