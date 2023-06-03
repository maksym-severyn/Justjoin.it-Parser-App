package com.example.justjoinparser.service.impl;

import com.example.justjoinparser.filter.City;
import com.example.justjoinparser.filter.PositionLevel;
import com.example.justjoinparser.model.Skill;
import com.example.justjoinparser.SkillsDictionary;
import com.example.justjoinparser.Sleeper;
import com.example.justjoinparser.filter.Technology;
import com.example.justjoinparser.service.PageService;
import com.example.justjoinparser.service.SkillService;
import com.example.justjoinparser.webdriver.Constants;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Service
@Slf4j
@RequiredArgsConstructor
class PageServiceReactiveImpl implements PageService {

    private static final int COUNT_OF_THREAD = 12;
    private static final String CITY = City.WROCLAW.getValue();
    private static final String POSITION_LEVEL = PositionLevel.MID.getValue();

    private final ApplicationContext applicationContext;
    private final SkillService skillService;

    @EventListener(ApplicationReadyEvent.class)
    public void parsePage() {

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        Set<String> hrefs = getOffersLinks(Technology.PHP, City.WROCLAW, PositionLevel.MID);

        long start = System.nanoTime();

        log.info("Found count of offers: {}", hrefs.size());
        Flux.fromIterable(hrefs)
            .flatMap(href ->
                Mono.fromCallable(() -> parseSkillsFromHref(href))
                    .subscribeOn(Schedulers.parallel())
                    .flatMapIterable(skills -> skills)
                    .flatMap(skill -> Flux.just(compareAndSetSkillNameAccordingWithDictionary(skill)))
            )
            .onErrorContinue(org.openqa.selenium.TimeoutException.class, (throwable, o) -> log.error(throwable.getMessage(), throwable))
            .flatMap(skillService::save)
            .doOnComplete(() -> log.info("Process of parse skills took: {} seconds", ((System.nanoTime() - start) / 1000000000)))
            .subscribe();
    }

    /**
     * en <p>
     *
     * In the first iteration, the loop goes from div[1] to div[11], then scrolls the page to div[11].
     * Immediately after scrolling, div[11] becomes div[3] (this is how the justjoin page works),
     * so the variable 'l' increments by 2 once.
     * From now on, the loop runs from div[3] to div[11 ] until it throws an exception that there is no element
     * <p>
     *
     * pl <p>
     *
     * W pierwszej iteracji pętla przechodzi od div[1] do div[11], następnie skroluje stronę do div[11].
     * Od razu po skrolowaniu, div[11] staje się div[3] (tak działa strona justjoin),
     * dlatego zmienna 'l' jednorozowo zwiększa się o 2. Od tego momentu, pętla działa w zakresie od div[3] do div[11],
     * dopóki nie poleci wyjątek że nie ma elementu
     */
    private Set<String> getOffersLinks(Technology technology, City city, PositionLevel positionLevel) {
        WebDriver driver = getWebDriverNewInstance(
            "https://justjoin.it/"
                + city.getValue()
                + "/"
                + technology.getValue()
                + "/"
                + positionLevel.getValue()
        );
        Set<String> links = new HashSet<>();

        boolean shouldOneTimeIncreaseJ = true;
        int l = 0;
        int lastSetSize = 0;
        WebElement element = null;
        while (true) {
            for (int j = 1 + l; j <= 11; j++) {
                lastSetSize = links.size();
                try {
                    element = driver.findElement(
                        By.xpath("//*[@id=\"root\"]/div[3]/div[1]/div/div[2]/div[1]/div/div/div[" + j + "]/div/a"));
                } catch (NoSuchElementException e) {
                    log.info("No more elements!");
                    break;
                }
                links.add(element.getAttribute("href"));
                if (shouldOneTimeIncreaseJ && j == 11) {
                    l = 2;
                    shouldOneTimeIncreaseJ = false;
                }
            }

            if (lastSetSize == links.size()) {
                log.debug("Link set has not been modified!");
                break;
            }

            ((JavascriptExecutor) driver)
                .executeScript("arguments[0].scrollIntoView(true);", element);
            Sleeper.sleep(220);
        }

        driver.quit();
        return links;
    }

    public List<Skill> parseSkillsFromHref(String href) {
        Assert.notNull(href, "input cannot be null");

        WebDriver myDriver = getWebDriverNewInstance(href);

        List<WebElement> elements = getElementsFromPage(myDriver, "css-1xm32e0");

        log.info("Elements collection size is: {}, offer: {}", elements.size(), href);
        List<Skill> skills = parseWebElementsIntoSkills(elements, href);

        myDriver.quit();
        return skills;
    }

    private List<Skill> parseWebElementsIntoSkills(List<WebElement> elements, String pageUrl) {
        return elements.stream()
            .map(webElement -> Skill.builder()
                .seniority(POSITION_LEVEL)
                .city(CITY)
                .name(webElement.findElement(By.xpath("./div[2]")).getText())
                .level(webElement.findElement(By.xpath("./div[3]")).getText())
                .offer(pageUrl)
                .build())
            .toList();
    }

    private Skill parseWebElementIntoSkills(WebElement element, String pageUrl) {
        return Skill.builder()
            .seniority(POSITION_LEVEL)
            .city(CITY)
            .name(element.findElement(By.xpath("./div[2]")).getText())
            .level(element.findElement(By.xpath("./div[3]")).getText())
            .offer(pageUrl)
            .build();
    }

    private Skill compareAndSetSkillNameAccordingWithDictionary(Skill skill) {
        String name = skill.getName().toLowerCase();
        for (Map.Entry<String, List<String>> entry : SkillsDictionary.DICTIONARY.entrySet()) {
            if (entry.getValue().contains(name)) {
                skill.setName(entry.getKey());
                name = skill.getName().toLowerCase();
            }
        }
        return skill;
    }

    private List<WebElement> getElementsFromPage(WebDriver openedPage, String elementClassName) {
        List<WebElement> elements = openedPage.findElements(By.className(elementClassName));
        while (elements.isEmpty()) {
            elements = openedPage.findElements(By.className(elementClassName));
        }
        return elements;
    }

    private WebDriver getWebDriverNewInstance(String url) {
        WebDriver driver = applicationContext.getBean(Constants.WEBDRIVER_BEAN_NAME, WebDriver.class);
        driver.get(url);
        driver.manage().window().setSize(new Dimension(900, 900));
        return driver;
    }
}
