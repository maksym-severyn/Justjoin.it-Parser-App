package com.example.justjoinparser.controller;

import com.example.justjoinparser.converter.OfferDtoToOfferFtoConverter;
import com.example.justjoinparser.fto.OfferFto;
import com.example.justjoinparser.fto.OfferParameterRequest;
import com.example.justjoinparser.service.OfferService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequiredArgsConstructor
@RequestMapping("/offer")
@Slf4j
public class JustjoinitOfferController {

    private final OfferService offerService;
    private final OfferDtoToOfferFtoConverter offerDtoToOfferFtoConverter;

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
        log.info("Start to parse offers for parameters: {}, {}, {}", request.technology(), request.seniority(),
            request.city());

        return ResponseEntity.ok(
            offerService.parseOffers(request.seniority(), request.city(), request.technology())
                .map(offerDtoToOfferFtoConverter::convertTo)
        );
    }
}
