package com.example.justjoinparser.controller;

import com.example.justjoinparser.converter.OfferDtoToOfferFtoConverter;
import com.example.justjoinparser.converter.RequestPositionLevelToPositionLevelConverter;
import com.example.justjoinparser.filter.City;
import com.example.justjoinparser.filter.Technology;
import com.example.justjoinparser.fto.OfferFto;
import com.example.justjoinparser.fto.RequestPositionLevel;
import com.example.justjoinparser.service.PageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequiredArgsConstructor
public class JustjoinitOfferController {

    private final PageService pageService;
    private final RequestPositionLevelToPositionLevelConverter positionConverter;
    private final OfferDtoToOfferFtoConverter offerDtoToOfferFtoConverter;

    @GetMapping(value = "/{city}/{technology}/{position}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public ResponseEntity<Flux<OfferFto>> parseOfferForParameters(@PathVariable City city,
                                                                  @PathVariable Technology technology,
                                                                  @PathVariable RequestPositionLevel position) {

        return ResponseEntity.ok(pageService.parseOffers(positionConverter.convertTo(position), city, technology)
                .map(offerDtoToOfferFtoConverter::convertTo));
    }
}
