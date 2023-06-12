package com.example.justjoinparser.util;

import lombok.RequiredArgsConstructor;
import org.openqa.selenium.WebDriver;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class WebDriverUtil {

    private final ApplicationContext applicationContext;

    public WebDriver getWebDriverNewInstance(String url) {
        WebDriver driver = applicationContext.getBean(WebDriver.class);
        driver.get(url);
        return driver;
    }
}
