package com.example.justjoinparser.exception;

public class CannotParseOffersRequest extends RuntimeException {

    public CannotParseOffersRequest(String message, Throwable cause) {
        super(message, cause);
    }
}
