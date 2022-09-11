package com.example.justjoinparser.webdriver;

import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeDriverLogLevel;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.Assert;

import java.nio.file.Paths;

@Configuration
@Slf4j
public class CustomWebDriver {

    @Value("${chrome.webdriver}")
    private String driverPath;

    public WebDriver getWebDriver() {
        WebDriver driver = null;
        setWebdriverProperty();
        try {
            driver = new ChromeDriver(getChromeOptions());
        } catch (Exception e) {
            log.error("Custom exception: cannot create ChromeDriver!");
        }
        Assert.notNull(driver, "Driver cannot be null!");
        return driver;
    }

    private ChromeOptions getChromeOptions() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--remote-debugging-port=9222", "--ignore-certificate-errors");
        options.addArguments("--headless");
        options.addArguments("--disable-gpu");
        options.addArguments("--no-sandbox");
        options.setLogLevel(ChromeDriverLogLevel.OFF);
        return options;
    }

    private void setWebdriverProperty() {
        if (Paths.get(driverPath).toFile().exists()) {
            System.setProperty("webdriver.chrome.driver", driverPath);
        } else {
            log.error(String.format(
                    "Driver specified by path %s does not exist. "
                            + "Place the driver in 'webdrivers' folder relative to application jar "
                            + "or start the application with argument %s specifying the webdriver location.",
                    driverPath, "driver.chrome"));
        }
    }

}