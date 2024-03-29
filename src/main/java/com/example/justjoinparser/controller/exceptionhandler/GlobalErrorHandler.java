package com.example.justjoinparser.controller.exceptionhandler;

import com.example.justjoinparser.exception.CannotParseOffersRequest;
import com.google.common.net.HttpHeaders;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class GlobalErrorHandler {

    @ExceptionHandler(CannotParseOffersRequest.class)
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    public ResponseEntity<ErrorCause> handleClientException(CannotParseOffersRequest ex) {
        log.error("Exception caught in handleClientException: {}", ex.getMessage(), ex);
        return ResponseEntity
            .status(HttpStatus.SERVICE_UNAVAILABLE)
            .headers(httpHeaders -> httpHeaders.add(HttpHeaders.RETRY_AFTER, "60"))
            .body(new ErrorCause(
                List.of(ex.getMessage())
            ));
    }
}
