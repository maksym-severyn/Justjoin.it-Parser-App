package com.example.justjoinparser.api.exception;

public class WebClientErrorAfterMultipleAttemptsException extends RuntimeException {
    public WebClientErrorAfterMultipleAttemptsException(String message, Throwable cause) {
        super(message, cause);
    }
}
