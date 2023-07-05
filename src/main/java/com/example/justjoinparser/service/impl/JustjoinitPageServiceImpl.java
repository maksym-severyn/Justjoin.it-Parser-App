package com.example.justjoinparser.service.impl;

import com.example.justjoinparser.SkillsDictionary;
import com.example.justjoinparser.exception.CannotParseOffersRequest;
import com.example.justjoinparser.util.WebDriverUtil;
import com.example.justjoinparser.dto.OfferDto;
import com.example.justjoinparser.dto.SkillDto;
import com.example.justjoinparser.filter.City;
import com.example.justjoinparser.filter.PositionLevel;
import com.example.justjoinparser.filter.Technology;
import com.example.justjoinparser.service.LinkService;
import com.example.justjoinparser.service.PageService;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;
import reactor.util.retry.Retry;

@Service
@Slf4j
@RequiredArgsConstructor
class JustjoinitPageServiceImpl implements PageService {

    private static final int CORES = Runtime.getRuntime().availableProcessors();
    private static final Scheduler DOUBLE_CORES_EXECUTOR_SCHEDULER
        = Schedulers.fromExecutor(Executors.newFixedThreadPool(CORES * 2));
    public static final Scheduler FIVE_THREAD_EXECUTOR_SCHEDULER
        = Schedulers.fromExecutorService(Executors.newFixedThreadPool(5), "fiveThreadEx");

    private final LinkService offerLinkService;
    private final WebDriverUtil webDriverUtil;

    @Value("${website-to-parse.justjoinit.skill.class-name}")
    private String skillClassName;

    @Override
    public Flux<OfferDto> parseOffers(PositionLevel positionLevel, City city, Technology technology) {
        Assert.noNullElements(new Object[] {positionLevel, city, technology},
            "input parameters (positionLevel, city, technology) cannot be null");

        return Flux.defer(() -> Flux.just(offerLinkService.getOfferLinks(technology, city, positionLevel)))
            .subscribeOn(FIVE_THREAD_EXECUTOR_SCHEDULER)
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
            .flatMapIterable(setFlux -> setFlux)
            .flatMap(href ->
                Mono.fromCallable(() -> parseOfferFromHref(href, positionLevel, city, technology))
                    .retryWhen(
                        Retry.fixedDelay(3, Duration.ofSeconds(3))
                            .doAfterRetry(rs -> log.info("Retry to extract elements from offer, attempt {}",
                                rs.totalRetries() + 1))
                            .onRetryExhaustedThrow((spec, rs) -> rs.failure())
                    )
                    .onErrorContinue(
                        TimeoutException.class,
                        (throwable, obj) -> log.info("Cannot extract elements collection"))
                    .map(this::renameSkillsIfNeed)
                    .subscribeOn(DOUBLE_CORES_EXECUTOR_SCHEDULER)
            )
            .onErrorContinue(
                TimeoutException.class,
                (throwable, obj) -> log.info("Cannot open page!%n%s".formatted(throwable.getMessage()), throwable))
            .doOnComplete(() ->
                    log.info("End of offers for request: {technology: {}, city: {}, position: {}})", technology, city, positionLevel));
    }

    private OfferDto parseOfferFromHref(String href, PositionLevel positionLevel, City city, Technology technology) {
        Assert.notNull(href, "href cannot be null");

        WebDriver myDriver = webDriverUtil.getWebDriverNewInstance(href);

        WebDriverWait wait = new WebDriverWait(myDriver, Duration.ofSeconds(3), Duration.ofSeconds(1));
        List<WebElement> elements = waitForElements(wait, href);

        log.info("Elements collection size is: {}, offer: {}", elements.size(), href);

        List<SkillDto> skills = parseWebElementsIntoSkills(elements);

        myDriver.quit();

        return OfferDto.builder()
            .skills(skills)
            .seniority(positionLevel)
            .city(city)
            .offerLink(href)
            .technology(technology)
            .build();
    }

    private List<WebElement> waitForElements(WebDriverWait waitDriver, String href) {
        try {
            return waitDriver.until(
                ExpectedConditions.presenceOfAllElementsLocatedBy(By.className(skillClassName))
            );
        } catch (TimeoutException e) {
            log.info("Elements collection not available for offer: {}. Try to retry...", href);
            throw new TimeoutException();
        }
    }

    private List<SkillDto> parseWebElementsIntoSkills(List<WebElement> elements) {
        Assert.notNull(elements, "elements cannot be null");

        return elements.stream()
            .map(webElement -> SkillDto.builder()
                .name(webElement.findElement(By.xpath("./div[2]")).getText())
                .level(webElement.findElement(By.xpath("./div[3]")).getText())
                .build())
            .toList();
    }

    private OfferDto renameSkillsIfNeed(OfferDto offerDto) {
        List<SkillDto> renamedSkills = new ArrayList<>();
        offerDto.skills().forEach(skill -> {
            SkillDto renamedSkill = compareAndSetSkillNameAccordingWithDictionary(skill);
            renamedSkills.add(renamedSkill);
        });
        return offerDto.toBuilder().skills(renamedSkills).build();
    }

    private SkillDto compareAndSetSkillNameAccordingWithDictionary(SkillDto skillDto) {
        String name = skillDto.name().toLowerCase();
        for (Map.Entry<String, List<String>> entry : SkillsDictionary.DICTIONARY.entrySet()) {
            if (entry.getValue().contains(name)) {
                skillDto = skillDto.toBuilder().name(entry.getKey()).build();
                break;
            }
        }
        return skillDto;
    }
}
