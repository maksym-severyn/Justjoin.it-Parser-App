package com.example.justjoinparser;

import com.example.justjoinparser.repo.PageRepo;
import com.example.justjoinparser.webdriver.CustomWebDriver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.output.NullOutputStream;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeDriverLogLevel;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.service.DriverService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
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

    @Value("${chrome.webdriver}")
    private String driverPath;

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
        ExecutorService threadPool = Executors.newFixedThreadPool(35);
        Flux.fromIterable(hrefs)
                .flatMap(href -> stringMono(href, threadPool))
                .doOnComplete(() -> log.info("Process of parse skills took: {} seconds",
                        ChronoUnit.SECONDS.between(start, LocalDateTime.now())))
                .subscribe();

        log.info("Main thread get finish");
    }

    Mono<String> stringMono(String href, ExecutorService executorService) {
        return Mono.fromCallable(() -> myNewOperation(href))
                .subscribeOn(Schedulers.fromExecutorService(executorService));
    }

    public String myNewOperation(String href) {
        WebDriver myDriver = null;
        try {
            // options
            System.setProperty("webdriver.chrome.driver", driverPath);
            ChromeOptions options = new ChromeOptions();
//            options.addArguments("--remote-debugging-port=9222");
            options.addArguments("--ignore-certificate-errors");
            options.addArguments("--headless");
            options.addArguments("--disable-gpu");
            options.addArguments("--no-sandbox");
            options.setLogLevel(ChromeDriverLogLevel.OFF);


            DriverService.Builder<ChromeDriverService, ChromeDriverService.Builder> serviceBuilder = new ChromeDriverService.Builder();
            ChromeDriverService chromeDriverService = serviceBuilder.build();
            chromeDriverService.sendOutputTo(NullOutputStream.NULL_OUTPUT_STREAM);

            myDriver = new ChromeDriver(chromeDriverService, options);

//            myDriver = new ChromeDriver(options);
            myDriver.get(href);

            Sleeper.sleepExactly(4);
            myDriver.manage().window().setSize(new Dimension(900, 900));
        } catch (Exception e) {
            log.error("Custom exception: cannot create ChromeDriver!", e.getMessage(), e);
        }

        // part of trying to get skills from page --- START
        List<WebElement> elements = myDriver.findElements(By.className("css-1xm32e0"));
        int counter = 1;
        while(elements.isEmpty() && counter <= 10) {
            log.warn("Try to rerun: {}/10 ...", counter);
            Sleeper.sleepExactly(3);
            elements = myDriver.findElements(By.className("css-1xm32e0"));
            counter++;
        }
        if (elements.isEmpty()) {
            log.error("Oops... No elements from offer: {}", href);
        }
        // part of trying to get skills from page --- END

        log.info("Elements collection size is: {}, offer: {}", elements.size(), href);
        List<Skill> skills = new ArrayList<>();
        elements.forEach(webElement -> {
            Skill mySkill = Skill.builder()
                    .seniority(POSITION_LEVEL)
                    .city(CITY)
                    .name(webElement.findElement(By.xpath("./div[2]")).getText())
                    .level(webElement.findElement(By.xpath("./div[3]")).getText())
                    .offer(href)
                    .build();
            skills.add(mySkill);
        });

        Map<String, String> dictionary = Map.ofEntries(
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
        List<Skill> updatedSkills = skills.stream()
                .map(skill -> {
                    String name = skill.getName().toLowerCase();
                    for (Map.Entry<String, String> entry : dictionary.entrySet()) {
                        if (name.contains(entry.getKey())) {
                            skill.setName(entry.getValue());
                            name = skill.getName().toLowerCase();
                        }
                    }
                    return skill;
                })
                .toList();
        pageRepo.saveAll(updatedSkills);

        return "Success!";
    }


    private WebDriver getWebDriver(String url) {
        WebDriver driver = customWebDriver.getWebDriver();
        driver.get(url);
        Sleeper.sleepExactly(4);
        return driver;
    }

}
