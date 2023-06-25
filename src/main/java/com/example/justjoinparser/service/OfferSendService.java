package com.example.justjoinparser.service;

import com.example.justjoinparser.amqp.publisher.MessageBrokerPublisher;
import com.example.justjoinparser.filter.City;
import com.example.justjoinparser.filter.PositionLevel;
import com.example.justjoinparser.filter.Technology;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

@Component
@RequiredArgsConstructor
@Slf4j
public class OfferSendService {

    private final PageService pageService;
    private final OfferService offerService;
    private final MessageBrokerPublisher messageBrokerPublisher;

    public void parseAndSend(PositionLevel positionLevel, City city, Technology technology) {
//        pageService.parseOffers(positionLevel, city, technology)
//            .flatMap(offerService::save)
//            .doOnNext(offerDto -> messageBrokerPublisher.sendMessage(
//                "offers.exchange", "offers."+ city.getValue() + "." + technology.getValue(), offerDto)
//            )
//            .onErrorContinue((error, cause) -> log.error("An error occurred during object handling", error))
//            .subscribe();

        Flux<Object> offerDtoFluxToBeSent = pageService.parseOffers(positionLevel, city, technology)
            .flatMap(offerService::save);

        messageBrokerPublisher.sendMessage("offers.exchange",
            "offers."+ city.getValue() + "." + technology.getValue(),
            offerDtoFluxToBeSent);
    }
}
