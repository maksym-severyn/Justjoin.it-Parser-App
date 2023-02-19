package com.example.justjoinparser;

import com.example.justjoinparser.repo.PageRepo;
import com.example.justjoinparser.webdriver.CustomWebDriver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.*;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeDriverLogLevel;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
@Slf4j
@RequiredArgsConstructor
@Profile("!default")
class PageServiceExecutorServiceImpl implements PageService {

    private static final String CITY = "wroclaw";
    private static final String POSITION_LEVEL = "junior";

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
                Sleeper.sleep(200);
            }
            if (infiniteHrefProtectCounter >= 200) { //zmień na 200!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
                break;
            }
        }

        ExecutorService executor = Executors.newFixedThreadPool(20);

        int o = 1;
        log.info("Set size: {}", hrefs.size());

        hrefs.forEach(href -> {
            executor.submit(() -> getSkillsFromPage(href, o));
            log.info("submit counter: {}", o);
        });

        executor.shutdown();
    }

    private void getSkillsFromPage(String href, int counter) {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--ignore-certificate-errors");
        options.addArguments("--headless");
        options.addArguments("--disable-gpu");
        options.addArguments("--no-sandbox");
        options.setLogLevel(ChromeDriverLogLevel.OFF);
        WebDriver myDriver = new ChromeDriver(options);
        myDriver.get(href);

        List<WebElement> elements = myDriver.findElements(By.className("css-1xm32e0"));
        while (elements.isEmpty()) {
            elements = myDriver.findElements(By.className("css-1xm32e0"));
        }
        log.info("elements size is: {}; Thread name: {}, counter: {}", elements.size(), Thread.currentThread().getName(), counter);
        elements.forEach(webElement -> {
            Skill mySkill = Skill.builder()
                    .seniority(POSITION_LEVEL)
                    .city(CITY)
                    .name(webElement.findElement(By.xpath("./div[2]")).getText())
                    .level(webElement.findElement(By.xpath("./div[3]")).getText())
                    .build();
            Skill save = pageRepo.save(mySkill);
            log.info("Skill {} saved!; counter: {}", save, counter);
        });
        myDriver.quit();
    }


    private WebDriver getWebDriver(String url) {
        WebDriver driver = customWebDriver.getWebDriver();
        driver.get(url);
        Sleeper.sleepExactly(3);
        return driver;
    }

}