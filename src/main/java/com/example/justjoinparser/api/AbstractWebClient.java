package com.example.justjoinparser.api;

import com.example.justjoinparser.api.exception.WebClientErrorAfterMultipleAttemptsException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.util.UriBuilder;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;
import reactor.util.retry.RetryBackoffSpec;

import java.net.URI;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Predicate;

@Slf4j
public abstract class AbstractWebClient {

    private final WebClient webClient;

    protected final Predicate<Throwable> is404ClientError = throwable ->
        throwable instanceof WebClientResponseException e && HttpStatus.NOT_FOUND.equals(e.getStatusCode());

    protected final Predicate<Throwable> isServer5xxError = throwable ->
        throwable instanceof WebClientResponseException e && e.getStatusCode().is5xxServerError();

    protected BiFunction<RetryBackoffSpec, Retry.RetrySignal, Throwable> onRetryError =
        (retryBackoffSpec, retrySignal) -> new WebClientErrorAfterMultipleAttemptsException(
            String.format("Request failed after %d attempts.", retryBackoffSpec.maxAttempts),
            retrySignal.failure());

    /**
     * Creates instances of WebClient.
     *
     * @param basePath api base path
     */
    protected AbstractWebClient(String basePath) {
        this.webClient = prepareWebClient(basePath).build();
    }

    /**
     * Performs HTTP GET request for a given path.
     *
     * @param typeReference class of return type
     * @param path          relative path (with path variables placeholders if any)
     * @param headers       headers that will be added to request
     * @param <T>           return type
     * @return Mono of type T
     */
    protected <T> Mono<T> getParametrized(ParameterizedTypeReference<T> typeReference, String path,
                                          HttpHeaders headers) {
        return webClient
            .get()
            .uri(uriBuilder -> this.buildUri(uriBuilder, path, List.of(), List.of()))
            .headers(httpHeaders -> httpHeaders.addAll(headers))
            .retrieve()
            .bodyToMono(typeReference);
    }

    protected <T> Mono<T> postParametrized(ParameterizedTypeReference<T> typeReference, String path,
                                          HttpHeaders headers, Object body) {
        return webClient
            .post()
            .uri(uriBuilder -> this.buildUri(uriBuilder, path, List.of(), List.of()))
            .bodyValue(body)
            .headers(httpHeaders -> httpHeaders.addAll(headers))
            .retrieve()
            .bodyToMono(typeReference);
    }

    private WebClient.Builder prepareWebClient(String basePath) {
        return WebClient.builder()
            .baseUrl(basePath);
    }

    private URI buildUri(UriBuilder uriBuilder, String path, List<String> queryParams, Object... uriVariables) {
        uriBuilder.path(path);
        queryParams.forEach(param -> uriBuilder.queryParam(param, String.format("{%s}", param)));
        return uriBuilder.build(uriVariables);
    }
}
