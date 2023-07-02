package com.example.justjoinparser.schedule;

import com.example.justjoinparser.filter.City;
import com.example.justjoinparser.filter.PositionLevel;
import com.example.justjoinparser.filter.Technology;
import com.example.justjoinparser.service.OfferSendService;
import lombok.RequiredArgsConstructor;
import net.javacrumbs.shedlock.core.LockAssert;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ParseOfferScheduler {

    private final OfferSendService offerSendService;

    @EventListener(ApplicationReadyEvent.class)
    @Scheduled(
        cron = "${scheduler.parse-offers.java.mid.cron}",
        zone = "${scheduler.parse-offers.java.mid.timezone}"
    )
    @SchedulerLock(name = "parseAndSendJavaMidOffersScheduler_task", lockAtMostFor = "15m", lockAtLeastFor = "5m")
    public void parseAndSendJavaMidOffersScheduler() {
        LockAssert.assertLocked();
        parseAndSendMidOffersScheduler(Technology.JAVA);
    }

    @Scheduled(
        cron = "${scheduler.parse-offers.python.mid.cron}",
        zone = "${scheduler.parse-offers.python.mid.timezone}"
    )
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
