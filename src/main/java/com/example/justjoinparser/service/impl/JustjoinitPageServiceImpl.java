package com.example.justjoinparser.service.impl;

import com.example.justjoinparser.SkillsDictionary;
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
import java.util.Set;
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
import reactor.core.scheduler.Schedulers;

@Service
@Slf4j
@RequiredArgsConstructor
class JustjoinitPageServiceImpl implements PageService {

    private final LinkService offerLinkService;
    private final WebDriverUtil webDriverUtil;

    @Value("${website-to-parse.justjoinit.skill.class-name}")
    private String skillClassName;

    @Override
    public Flux<OfferDto> parseOffers(PositionLevel positionLevel, City city, Technology technology) {
        Assert.noNullElements(new Object[] {positionLevel, city, technology},
            "input parameters (positionLevel, city, technology) cannot be null");

        Set<String> hrefs = offerLinkService.getOfferLinks(technology, city, positionLevel);
        log.info("Found count of offers: {}", hrefs.size());

        return Flux.fromIterable(hrefs)
            .flatMap(href ->
                Mono.fromCallable(() -> parseOfferFromHref(href, positionLevel, city, technology))
                    .subscribeOn(Schedulers.parallel())
                    .map(this::renameSkillsIfNeed)
            )
            .onErrorContinue(TimeoutException.class, (throwable, obj) -> log.error("Cannot open page!\n"
                + throwable.getMessage(), throwable));
    }

    private OfferDto parseOfferFromHref(String href, PositionLevel positionLevel, City city, Technology technology) {
        Assert.notNull(href, "href cannot be null");

        WebDriver myDriver = webDriverUtil.getWebDriverNewInstance(href);

        WebDriverWait wait = new WebDriverWait(myDriver, Duration.ofSeconds(3));
        List<WebElement> elements = waitForElements(wait);

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

    private List<WebElement> waitForElements(WebDriverWait waitDriver) {
        List<WebElement> elements = new ArrayList<>();
        try {
            elements = waitDriver.until(
                ExpectedConditions.presenceOfAllElementsLocatedBy(By.className(skillClassName))
            );
        } catch (TimeoutException e) {
            log.warn("Elements collection not available");
        }
        return elements;
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
