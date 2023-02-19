package com.example.justjoinparser;

import com.example.justjoinparser.repo.PageRepo;
import javax.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.*;
import org.openqa.selenium.NoSuchElementException;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
@Slf4j
@RequiredArgsConstructor
class PageServiceReactiveImpl implements PageService {

    private static final Map<String, List<String>> DICTIONARY = Map.ofEntries(
        Map.entry("REST", List.of("rest")),
        Map.entry("Spring", List.of("spring boot", "spring framework")),
        Map.entry("CI/CD", List.of("ci", "ci/cd (jenkins);")),
        Map.entry("Java", List.of("java 8+", "java 8", "java 11", "Java11")),
        Map.entry("WebServices", List.of("webservice", "webservices")),
        Map.entry("Microservices", List.of("microservice")),
        Map.entry("Angular", List.of("angular")),
        Map.entry("HTML", List.of("html")),
        Map.entry("Hibernate", List.of("hibernate", "jpa")),
        Map.entry("AWS", List.of("amazon", "aws")),
        Map.entry("SQL", List.of("database", "bazy danych", "sql server")),
        Map.entry("Git", List.of("github", "gitlab", "git lab", "git/bitbucket")),
        Map.entry("NoSQL", List.of("mongodb")),
        Map.entry("PostgreSQL", List.of("postresql", "postgre sql"))
    );

    private static final int COUNT_OF_THREAD = 15;
    private static final String CITY = "all";
    private static final String POSITION_LEVEL = "mid";
    private final ApplicationContext applicationContext;
    private final PageRepo pageRepo;

    /**
     * Jak działa pętla while() :
     * <p>
     * W pierwszej iteracji pętla leci od div[1] do div[11], następnie skroluje stronę do div[11].
     * Od razu po skrolowaniu, div[11] staje się div[3] (tak działa strona justjoin),
     * dlatego zmienna 'l' jednorozowo zwiększa się o +2, żeby pętla zaczęła chodzić od div[3] do div[11],
     * i następnie znów od div[3] do div[11], dopóki nie poleci wyjątek że nie ma elementu
     */
    @EventListener(ApplicationReadyEvent.class)
    public void parsePage() {
        WebDriver driver = getWebDriver("https://justjoin.it/" + CITY + "/java/" + POSITION_LEVEL, 4);
        Set<String> hrefs = new HashSet<>();

        boolean shouldProcess = true;
        boolean shouldOneTimeIncreaseJ = true;
        int l = 0;
        int infiniteHrefProtectCounter = 0;
        WebElement element = null;
        while (shouldProcess) {
            for (int j = 1 + l; j <= 11; j++) {
                try {
                    element = driver.findElement(By.xpath("//*[@id=\"root\"]/div[3]/div[1]/div/div[2]/div[1]/div/div/div[" + j + "]/div/a"));
                } catch (NoSuchElementException e) {
                    log.info("No more elements!");
                    shouldProcess = false;
                    break;
                }
                hrefs.add(element.getAttribute("href"));
                if (shouldOneTimeIncreaseJ && j == 11) {
                    l = 2;
                    shouldOneTimeIncreaseJ = false;
                }
                infiniteHrefProtectCounter++;
            }
            if (shouldProcess) {
                ((JavascriptExecutor) driver)
                        .executeScript("arguments[0].scrollIntoView(true);", element);
                Sleeper.sleep(220);
            }
            if (infiniteHrefProtectCounter >= 1000) { //zmień na 200!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
                log.warn("Infinite breaker activate!");
                break;
            }
        }

        driver.quit();
        LocalDateTime start = LocalDateTime.now();

        log.info("Found count of hrefs: {}", hrefs.size());
        ExecutorService threadPool = Executors.newFixedThreadPool(COUNT_OF_THREAD);
        Flux.fromIterable(hrefs)
                .flatMap(href -> Mono.fromRunnable(() -> parseAndSaveSkillsFromHref(href))
                        .subscribeOn(Schedulers.fromExecutorService(threadPool))
//                        .subscribeOn(Schedulers.boundedElastic())
//                        .subscribeOn(Schedulers.parallel())
                        .then())
                .doOnComplete(() -> log.info("Process of parse skills took: {} seconds",
                    ChronoUnit.SECONDS.between(start, LocalDateTime.now())))
                .subscribe();

        log.info("Main thread get finish");
        log.info("Core in machine: {}", Runtime.getRuntime().availableProcessors());
    }

    public void parseAndSaveSkillsFromHref(String href) {
        Assert.notNull(href, "input cannot be null");

        WebDriver myDriver = getWebDriver(href, 1);

        List<WebElement> elements = getElementsFromPage(myDriver, "css-1xm32e0");

        log.info("Elements collection size is: {}, offer: {}", elements.size(), href);
        List<Skill> skills = parseWebElementsIntoSkills(elements, href);

        myDriver.quit();

        pageRepo.saveAll(
                skills.stream()
                        .map(this::compareAndSetSkillNameAccordingWithDictionary)
                        .toList()
        );
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

    private Skill compareAndSetSkillNameAccordingWithDictionary(Skill skill) {
        String name = skill.getName().toLowerCase();
        for (Map.Entry<String, List<String>> entry : DICTIONARY.entrySet()) {
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

    private WebDriver getWebDriver(String url, @Nullable Integer sleepSec) {
        WebDriver driver = applicationContext.getBean("webDriver", WebDriver.class);
        driver.get(url);
        if (sleepSec != null) {
            Sleeper.sleep(10);
        }
        driver.manage().window().setSize(new Dimension(900, 900));
        return driver;
    }
}
