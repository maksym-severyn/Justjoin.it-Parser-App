package com.example.justjoinparser.service.impl;

import com.example.justjoinparser.api.client.OfferParserClient;
import com.example.justjoinparser.dto.OfferDto;
import com.example.justjoinparser.exception.CannotParseOffersRequest;
import com.example.justjoinparser.filter.City;
import com.example.justjoinparser.filter.PositionLevel;
import com.example.justjoinparser.filter.Technology;
import com.example.justjoinparser.service.OfferCatalogParser;
import com.example.justjoinparser.service.OfferLinkService;
import com.example.justjoinparser.service.OfferService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.Executors;

@Component
@RequiredArgsConstructor
@Slf4j
class OfferServiceImpl implements OfferService {

    public static final Scheduler FIVE_THREAD_EXECUTOR_SCHEDULER
        = Schedulers.fromExecutorService(Executors.newFixedThreadPool(5), "fiveThreadEx");

    private final OfferCatalogParser offerOfferCatalogParser;
    private final OfferLinkService offerLinkService;
    private final OfferParserClient offerParserClient;

    @Override
    public Flux<OfferDto> parseOffers(PositionLevel positionLevel, City city, Technology technology) {
        Assert.noNullElements(new Object[]{positionLevel, city, technology},
            "input parameters (positionLevel, city, technology) cannot be null");

        return Flux.just(offerOfferCatalogParser.getOffersLinks(technology, city, positionLevel))
//            .subscribeOn(FIVE_THREAD_EXECUTOR_SCHEDULER)
            .retryWhen(
                Retry.fixedDelay(5, Duration.ofSeconds(5))
                    .filter(ex -> ex instanceof org.openqa.selenium.NoSuchElementException ||
                        ex instanceof org.springframework.beans.factory.BeanCreationException ||
                        ex instanceof org.openqa.selenium.StaleElementReferenceException ||
                        ex instanceof org.openqa.selenium.TimeoutException)
                    .doAfterRetry(rs -> log.info("Retry to get offers for request: {}, {}, {}; attempt {}",
                        technology, city, positionLevel, rs.totalRetries() + 1))
                    .onRetryExhaustedThrow((spec, rs) -> rs.failure())
            )
            .onErrorMap(throwable -> new CannotParseOffersRequest(
                "Cannot get links to offers with provided parameters: %s, %s, %s. The page temporarily unavailable. Try again later"
                    .formatted(technology, city, positionLevel),
                throwable)
            )
            .doOnNext(hrefs -> log.info("Found count of offers: {} (technology: {}, city: {}, position: {})",
                hrefs.size(), technology, city, positionLevel))
            .flatMapIterable(links -> links)
            .flatMap(link -> offerLinkService.save(link, positionLevel, city, technology))
            .flatMap(offerParserClient::parseOffer);
//            .flatMap(offerLinkDto -> Mono.just(OfferDto.builder().offerLink(offerLinkDto.link()).build()));

//        messageBrokerPublisher.sendMessage("offers.exchange",
//            "offers." + city.getFilterValue() + "." + technology.getFilterValue(),
//            offerDtoFluxToBeSent);
    }
}
