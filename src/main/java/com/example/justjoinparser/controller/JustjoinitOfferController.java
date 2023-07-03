package com.example.justjoinparser.controller;

import com.example.justjoinparser.converter.OfferDtoToOfferFtoConverter;
import com.example.justjoinparser.converter.RequestPositionLevelToPositionLevelConverter;
import com.example.justjoinparser.filter.City;
import com.example.justjoinparser.filter.Technology;
import com.example.justjoinparser.fto.OfferFto;
import com.example.justjoinparser.fto.OfferParameterRequest;
import com.example.justjoinparser.fto.RequestPositionLevel;
import com.example.justjoinparser.service.OfferService;
import com.example.justjoinparser.service.PageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;

@RestController
@RequiredArgsConstructor
public class JustjoinitOfferController {

    private final PageService pageService;
    private final OfferService offerService;
    private final RequestPositionLevelToPositionLevelConverter positionConverter;
    private final OfferDtoToOfferFtoConverter offerDtoToOfferFtoConverter;

    @PostMapping(value = "/", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public ResponseEntity<Flux<OfferFto>> initializeOfferParsingWithProvidedParams(
            @RequestBody OfferParameterRequest request) {

        return ResponseEntity.ok(pageService.parseOffers(
                        positionConverter.convertTo(request.seniority()),
                        request.city(),
                        request.technology()
                )
                .flatMap(offerService::save)
                .map(offerDtoToOfferFtoConverter::convertTo));
    }

    @GetMapping("/top/{topCounter}")
    public Mono<ResponseEntity<Map<String, Long>>> getExistingSkillsSkills(@PathVariable Long topCounter,
                                                             @RequestParam City city,
                                                             @RequestParam Technology technology,
                                                             @RequestParam RequestPositionLevel position) {

        return offerService.findTopSkillsByParameters(topCounter, positionConverter.convertTo(position), city, technology)
                .filter(sortedSkillsMap -> !sortedSkillsMap.isEmpty())
                .map(ResponseEntity::ok)
                .switchIfEmpty(Mono.just(ResponseEntity.notFound().build()));
    }
}
