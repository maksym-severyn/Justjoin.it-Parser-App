package com.example.justjoinparser.api.client.impl;

import com.example.justjoinparser.api.AbstractWebClient;
import com.example.justjoinparser.api.client.OfferParserClient;
import com.example.justjoinparser.dto.OfferDto;
import com.example.justjoinparser.dto.OfferLinkDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;

@Component
@Slf4j
class OfferParserClientImpl extends AbstractWebClient implements OfferParserClient {

    private static final String POST_OFFER_PATH = "/offer";

    protected OfferParserClientImpl(@Value("${api.offer-parser}") final String basePath) {
        super(basePath);
    }

    @Override
    public Mono<OfferDto> parseOffer(OfferLinkDto offerLink) {
        return postParametrized(
            new ParameterizedTypeReference<OfferDto>() {},
            POST_OFFER_PATH,
            new HttpHeaders(),
            offerLink
        ).retryWhen(Retry.backoff(3, Duration.ofSeconds(2))
            .filter(isServer5xxError)
            .doBeforeRetry(rs -> log.info("Try to retry to parse offer: \"{}\"; attempt {}", offerLink.link(),
                rs.totalRetries() + 1))
            .onRetryExhaustedThrow(onRetryError)
        );
    }

}
