package com.example.justjoinparser.service.impl;

import com.example.justjoinparser.util.WebDriverUtil;
import com.example.justjoinparser.filter.City;
import com.example.justjoinparser.filter.PositionLevel;
import com.example.justjoinparser.filter.Technology;
import com.example.justjoinparser.service.LinkService;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
class JustjointLinkServiceImpl implements LinkService {

    private final WebDriverUtil webDriverUtil;

    @Value("${websiteToParse.justjoinit.domain}")
    private String justjoinitDomain;

    @Value("${websiteToParse.justjoinit.divToOfferList}")
    private String parentDivPath;

    @Value("${websiteToParse.justjoinit.offer.height}")
    private String offerBoxHeight;

    @Value("${websiteToParse.justjoinit.offer.endOfOffersMessageAnnouncing}")
    private String endOfOffersMessageAnnouncing;

    @Override
    public Set<String> getOfferLinks(Technology technology, City city, PositionLevel positionLevel) {
        WebDriver driver = webDriverUtil.getWebDriverNewInstance(
            String.format("%s/%s/%s/%s",
                justjoinitDomain,
                city.getValue(),
                technology.getValue(),
                positionLevel.getValue())
        );

        WebElement parentDiv =
            driver.findElement(By.xpath(parentDivPath));

        Set<String> hrefSet = new HashSet<>();

        boolean continueLoop = true;
        while (continueLoop) {
            List<WebElement> childDivs = parentDiv.findElements(By.xpath("./div"));

            WebElement linkElement = null;
            for (WebElement childDiv : childDivs) {
                String height = childDiv.getCssValue("height");

                if (height.equals(offerBoxHeight)) {
                    linkElement = childDiv.findElement(By.tagName("a"));
                    String href = linkElement.getAttribute("href");
                    hrefSet.add(href);
                } else {
                    WebElement pElement;
                    try {
                        pElement = childDiv.findElement(By.xpath("./div/p"));
                        String text = pElement.getText();

                        if (text.equals(endOfOffersMessageAnnouncing)) {
                            log.info("Found end of offer list");
                        }
                        continueLoop = false;
                    } catch (NoSuchElementException e) {
                        log.warn(
                            "Cannot be sure about and of offer list! Last box has height: {}. Should check justjoin.it page",
                            height);
                        continueLoop = false;
                    }
                    break;
                }
            }

            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", linkElement);
        }

        driver.quit();
        return hrefSet;
    }
}
