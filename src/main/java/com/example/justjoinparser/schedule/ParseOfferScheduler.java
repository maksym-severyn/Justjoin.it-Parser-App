package com.example.justjoinparser.schedule;

import com.example.justjoinparser.filter.City;
import com.example.justjoinparser.filter.PositionLevel;
import com.example.justjoinparser.filter.Technology;
import com.example.justjoinparser.service.OfferSendService;
import com.example.justjoinparser.service.OfferService;
import java.util.ArrayList;
import java.util.List;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.core.LockAssert;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ParseOfferScheduler {

    private final List<City> cityParametersToParseOffers = new ArrayList<>();
    private final List<PositionLevel> positionParametersToParseOffers = new ArrayList<>();
    private final List<Technology> technologyParametersToParseOffers = new ArrayList<>();

    private final OfferSendService offerSendService;
    private final OfferService offerService;
    private final ScheduleProperties scheduleProperties;

    @PostConstruct
    private void initParametersToParseOffers() {
        scheduleProperties.getCities().forEach(city ->
            cityParametersToParseOffers.add(City.getFromValueFto(city)));
        scheduleProperties.getSeniority().forEach(position ->
            positionParametersToParseOffers.add(PositionLevel.getFromValueFto(position)));
        scheduleProperties.getTechnologies().forEach(technology ->
            technologyParametersToParseOffers.add(Technology.getFromValueFto(technology)));
    }

    @EventListener(ApplicationReadyEvent.class)
    @SchedulerLock(name = "initOffersParseAndSendScheduler_task", lockAtMostFor = "5m", lockAtLeastFor = "5m")
    public void initOffersParseAndSendScheduler() {
        LockAssert.assertLocked();
        offerService.count()
            .doOnNext(size -> log.info("Currently there are {} offers in database", size))
            .filter(size -> size == 0)
            .doOnNext(size -> log.info("No offers found, initializing the scheduler for parsing and sending offers"))
            .doOnError(error -> log.error("Error occurred while initializing scheduler: ", error))
            .subscribe(size -> parseAndSendPredefinedOffers());
    }


    @Scheduled(cron = "${scheduler.parse-offers.cron}", zone = "${scheduler.parse-offers.timezone}")
    @SchedulerLock(name = "parseAndSendAllOffersScheduler_task", lockAtMostFor = "60m", lockAtLeastFor = "60m")
    public void parseAndSendAllOffersScheduler() {
        LockAssert.assertLocked();
        for (Technology technology : Technology.values()) {
            for (City city : City.values()) {
                if (City.ALL.equals(city)) {
                    continue;
                }
                for (PositionLevel position : PositionLevel.values()) {
                    if (PositionLevel.ALL.equals(position)) {
                        continue;
                    }
                    offerSendService.parseAndSend(position, city, technology);
                }
            }
        }
    }


    private void parseAndSendPredefinedOffers() {
        for (City city : cityParametersToParseOffers) {
            for (Technology technology : technologyParametersToParseOffers) {
                for (PositionLevel positionLevel : positionParametersToParseOffers) {
                    offerSendService.parseAndSend(positionLevel, city, technology);
                }
            }
        }
    }
}
