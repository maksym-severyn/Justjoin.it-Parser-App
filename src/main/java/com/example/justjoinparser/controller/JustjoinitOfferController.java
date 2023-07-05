package com.example.justjoinparser.controller;

import com.example.justjoinparser.converter.MapSkillToSortedListSkillConverter;
import com.example.justjoinparser.converter.OfferDtoToOfferFtoConverter;
import com.example.justjoinparser.filter.City;
import com.example.justjoinparser.filter.PositionLevel;
import com.example.justjoinparser.filter.Technology;
import com.example.justjoinparser.fto.OfferFto;
import com.example.justjoinparser.fto.OfferParameterRequest;
import com.example.justjoinparser.fto.TopSkillFto;
import com.example.justjoinparser.service.OfferService;
import com.example.justjoinparser.service.PageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
@RequestMapping("/offer")
public class JustjoinitOfferController {

    private final PageService pageService;
    private final OfferService offerService;
    private final OfferDtoToOfferFtoConverter offerDtoToOfferFtoConverter;
    private final MapSkillToSortedListSkillConverter mapSkillToSortedListSkillConverter;

    @Operation(summary = "Start to parse actual offers from justjoin.it portal, for given parameters, save it to DB and return")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Offers parsed and saved",
            content = {
                @Content(
                    mediaType = "text/event-stream",
                    array = @ArraySchema(schema = @Schema(implementation = OfferFto.class))
                )
            }
        )
    })
    @PostMapping(value = "/actual", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public ResponseEntity<Flux<OfferFto>> initializeOfferParsingWithProvidedParams(
        @RequestBody OfferParameterRequest request) {

        return ResponseEntity.ok(
            pageService.parseOffers(
                    request.seniority(), request.city(), request.technology()
                )
                .flatMap(offerService::save)
                .map(offerDtoToOfferFtoConverter::convertTo)
        );
    }

    @GetMapping("/skill/existing/top/{topCounter}")
    public Mono<ResponseEntity<List<TopSkillFto>>> getExistingSkillsSkills(@PathVariable Long topCounter,
                                                                           @RequestParam City city,
                                                                           @RequestParam Technology technology,
                                                                           @RequestParam PositionLevel position) {
        return offerService.findTopSkillsByParameters(topCounter, position, city, technology)
            .filter(sortedSkillsMap -> !sortedSkillsMap.isEmpty())
            .map(mapSkillToSortedListSkillConverter::convertTo)
            .map(ResponseEntity::ok)
            .switchIfEmpty(Mono.just(ResponseEntity.notFound().build()));
    }
}
