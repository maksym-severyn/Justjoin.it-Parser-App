package com.example.justjoinparser.webdriver;

import java.net.MalformedURLException;
import java.net.URL;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriverLogLevel;
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

    @Bean("webDriver")
    @Scope("prototype")
    public WebDriver getWebDriver() {
        WebDriver driver = null;
        try {
            driver = new RemoteWebDriver(new URL(driverPath), getChromeOptions());
        } catch (MalformedURLException e) {
            log.error("Custom exception: cannot create ChromeDriver!", e);
        }
        Assert.notNull(driver, "Driver cannot be null!");
        return driver;
    }

    private ChromeOptions getChromeOptions() {
        ChromeOptions options = new ChromeOptions();
//        options.addArguments("--remote-debugging-port=9222");
        options.addArguments("--ignore-certificate-errors");
        options.addArguments("--headless");
        options.addArguments("--disable-gpu");
        options.addArguments("--no-sandbox");
        options.setLogLevel(ChromeDriverLogLevel.OFF);
        return options;
    }

}