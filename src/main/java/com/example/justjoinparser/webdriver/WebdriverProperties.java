package com.example.justjoinparser.webdriver;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "webdriver")
@Getter
@Setter
public class WebdriverProperties {

    private String scheme;
    private String host;
    private String port;
    private String path;

    public String getWebDriverFullPath() {
        return new StringBuilder(scheme)
            .append("://")
            .append(host)
            .append(":")
            .append(port)
            .append(path)
            .toString();
    }
}
