package com.example.justjoinparser.webdriver;

import java.net.MalformedURLException;
import java.net.URL;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.PageLoadStrategy;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.util.Assert;

@Configuration
@Slf4j
public class CustomWebDriver {

    @Value("${webdriver.host}")
    private String driverPath;

    @Bean(Constants.WEBDRIVER_BEAN_NAME)
    @Scope("prototype")
    public WebDriver getWebDriver() {
        WebDriver driver = null;
        try {
            driver = new RemoteWebDriver(new URL(driverPath), getChromeOptions());
        } catch (MalformedURLException e) {
            log.error("Custom exception: cannot create ChromeDriver!", e);
        }
        Assert.notNull(driver, "Driver cannot be null!");
        enableStealth(driver);
        return driver;
    }

    private ChromeOptions getChromeOptions() {
        ChromeOptions options = new ChromeOptions();
//        options.addArguments("--remote-debugging-port=9222");
        options.addArguments("--ignore-certificate-errors");
        options.addArguments("--headless");
        options.addArguments("--disable-gpu");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-blink-features=AutomationControlled");
        options.addArguments("--disable-extensions");
        options.addArguments(
            "user-agent=Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/107.0.0.0 Safari/537.36");
        options.setPageLoadStrategy(PageLoadStrategy.NORMAL);
        return options;
    }

    private void enableStealth(WebDriver driver) {
        JavascriptExecutor js = (JavascriptExecutor) driver;
        js.executeScript("Object.defineProperty(navigator, 'webdriver', {get: () => undefined})");
    }
}