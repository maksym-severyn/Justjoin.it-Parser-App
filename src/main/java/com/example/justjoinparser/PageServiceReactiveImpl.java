package com.example.justjoinparser;

import com.example.justjoinparser.repo.PageRepo;
import com.example.justjoinparser.webdriver.CustomWebDriver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.output.NullOutputStream;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeDriverLogLevel;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.service.DriverService;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
@Slf4j
@RequiredArgsConstructor
class PageServiceReactiveImpl implements PageService {

    private static final Map<String, String> DICTIONARY = Map.ofEntries(
            Map.entry("rest", "REST"),
            Map.entry("webservice", "WebServices"),
            Map.entry("web services", "WebServices"),
            Map.entry("microservice", "Microservices"),
            Map.entry("angular", "Angular"),
            Map.entry("html", "HTML"),
            Map.entry("hibernate", "Hibernate"),
            Map.entry("jpa", "Hibernate"),
            Map.entry("amazon", "AWS"),
            Map.entry("aws", "AWS"),
            Map.entry("bazy danych", "SQL"),
            Map.entry("database", "SQL"),
            Map.entry("github", "Git"),
            Map.entry("Gitlab", "Git"),
            Map.entry("mongodb", "NoSQL"),
            Map.entry("postresql", "PostgreSQL")
    );

    private static final int COUNT_OF_THREAD = 30;
    private static final String CITY = "wroclaw";
    private static final String POSITION_LEVEL = "mid";

    private final CustomWebDriver customWebDriver;
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
        WebDriver driver = getWebDriver("https://justjoin.it/" + CITY + "/java/" + POSITION_LEVEL);
        driver.manage().window().setSize(new Dimension(900, 900));
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
            if (infiniteHrefProtectCounter >= 500) { //zmień na 200!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
                break;
            }
        }

        driver.quit();
        LocalDateTime start = LocalDateTime.now();

        log.info("Found count of hrefs: {}", hrefs.size());
        ExecutorService threadPool = Executors.newFixedThreadPool(COUNT_OF_THREAD);
        Flux.fromIterable(hrefs)
                .flatMap(href -> concurrencyParseAndSaveSkillsFromHref(href, threadPool))
                .doOnComplete(() -> log.info("Process of parse skills took: {} seconds",
                        ChronoUnit.SECONDS.between(start, LocalDateTime.now())))
                .subscribe();

        log.info("Main thread get finish");
    }

    Mono<Void> concurrencyParseAndSaveSkillsFromHref(String href, ExecutorService executorService) {
        return Mono.fromRunnable(() -> parseAndSaveSkillsFromHref(href))
                .subscribeOn(Schedulers.fromExecutorService(executorService))
                .then();
    }

    public void parseAndSaveSkillsFromHref(String href) {
        Assert.notNull(href, "input cannot be null");

        WebDriver myDriver = openPage(href);

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
        for (Map.Entry<String, String> entry : DICTIONARY.entrySet()) {
            if (name.contains(entry.getKey())) {
                skill.setName(entry.getValue());
                name = skill.getName().toLowerCase();
            }
        }
        return skill;
    }

    private List<WebElement> getElementsFromPage(WebDriver openedPage, String elementClassName) {
        List<WebElement> elements = openedPage.findElements(By.className(elementClassName));
        int counter = 1;
        while (elements.isEmpty() && ++counter <= 10) {
            log.warn("Try to rerun: {}/10 ...", counter);
            Sleeper.sleepExactly(2);
            elements = openedPage.findElements(By.className(elementClassName));
        }
        if (elements.isEmpty()) {
            log.error("Oops... No elements from page: {}", openedPage.getCurrentUrl());
        }
        return elements;
    }

    private WebDriver openPage(String href) {
        try {
            WebDriver myDriver = createNewWebDriver();
            myDriver.get(href);
            Sleeper.sleepExactly(4);
            myDriver.manage().window().setSize(new Dimension(900, 900));
            return myDriver;
        } catch (Exception e) {
            log.error("WebDriver initialization failed! WebDriver is null", e.getMessage(), e);
            throw e;
        }
    }

    private WebDriver createNewWebDriver() {
        DriverService.Builder<ChromeDriverService, ChromeDriverService.Builder> serviceBuilder = new ChromeDriverService.Builder();
        ChromeDriverService chromeDriverService = serviceBuilder.build();
        chromeDriverService.sendOutputTo(NullOutputStream.NULL_OUTPUT_STREAM);
        return new ChromeDriver(chromeDriverService, getAnotherOptions());
    }

    private ChromeOptions getAnotherOptions() {
        ChromeOptions options = new ChromeOptions();
//        options.addArguments("--remote-debugging-port=9222");
        options.addArguments("--ignore-certificate-errors");
        options.addArguments("--headless");
        options.addArguments("--disable-gpu");
        options.addArguments("--no-sandbox");
        options.setLogLevel(ChromeDriverLogLevel.OFF);
        return options;
    }


    private WebDriver getWebDriver(String url) {
        WebDriver driver = customWebDriver.getWebDriver();
        driver.get(url);
        Sleeper.sleepExactly(4);
        return driver;
    }

}
