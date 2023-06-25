package com.example.justjoinparser.controller;

import com.example.justjoinparser.filter.City;
import com.example.justjoinparser.filter.PositionLevel;
import com.example.justjoinparser.filter.Technology;
import com.example.justjoinparser.service.OfferSendService;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class TestController {

    private final OfferSendService offerSendService;

    @GetMapping(value = {
        "/{city}/{technology}/{position}",
        "/{city}/{technology}"
    })
    public ResponseEntity<String> parseController(
        @PathVariable(name = "city") City city,
        @PathVariable(name = "technology") String technology,
        @PathVariable(required = false, name = "position") Optional<String> position
    ) {
        offerSendService.parseAndSend(
            PositionLevel.getFromValue(position.orElse("")),
            City.getFromValue(city.getValue()),
            Technology.getFromValue(technology));
        return ResponseEntity.ok("Done!");
    }
}
