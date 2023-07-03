package com.example.justjoinparser.schedule;

import com.example.justjoinparser.filter.City;
import com.example.justjoinparser.filter.PositionLevel;
import com.example.justjoinparser.filter.Technology;
import com.example.justjoinparser.service.OfferSendService;
import com.example.justjoinparser.service.OfferService;
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

    private final OfferSendService offerSendService;
    private final OfferService offerService;

    @EventListener(ApplicationReadyEvent.class)
    @SchedulerLock(name = "initJavaMidOffersScheduler_task", lockAtMostFor = "2m", lockAtLeastFor = "2m")
    public void initJavaMidOffersScheduler() {
        LockAssert.assertLocked();
        offerService.count()
                .doOnNext(size -> log.info("Currently there are {} offers in database", size))
                .filter(size -> size == 0)
                .doOnNext(size -> log.info("No offers found, initializing the scheduler for parsing and sending offers"))
                .doOnError(error -> log.error("Error occurred while initializing scheduler: ", error))
                .subscribe(size -> parseAndSendMidOffersScheduler(Technology.JAVA));
    }

    @Scheduled(cron = "${scheduler.parse-offers.java.mid.cron}", zone = "${scheduler.parse-offers.java.mid.timezone}")
    @SchedulerLock(name = "parseAndSendJavaMidOffersScheduler_task", lockAtMostFor = "15m", lockAtLeastFor = "5m")
    public void parseAndSendJavaMidOffersScheduler() {
        LockAssert.assertLocked();
        parseAndSendMidOffersScheduler(Technology.JAVA);
    }

    @Scheduled(cron = "${scheduler.parse-offers.python.mid.cron}", zone = "${scheduler.parse-offers.python.mid.timezone}")
    @SchedulerLock(name = "parseAndSendPythonMidOffersScheduler_task", lockAtMostFor = "15m", lockAtLeastFor = "5m")
    public void parseAndSendPythonMidOffersScheduler() {
        LockAssert.assertLocked();
        parseAndSendMidOffersScheduler(Technology.PYTHON);
    }

    private void parseAndSendMidOffersScheduler(Technology technology) {
        for (City city : City.values()) {
            if (City.ALL.equals(city)) {
                continue;
            }
            offerSendService.parseAndSend(PositionLevel.MID, city, technology);
        }
    }
}
