package com.example.justjoinparser.util;

import lombok.RequiredArgsConstructor;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.WebDriver;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class WebDriverUtil {

    private final ApplicationContext applicationContext;
    private static final Dimension DIMENSION = new Dimension(900, 1080);

    public WebDriver getWebDriverNewInstance(String url) {
        WebDriver driver = applicationContext.getBean(WebDriver.class);
        driver.manage().window().setSize(DIMENSION);
        driver.get(url);
        return driver;
    }
}
