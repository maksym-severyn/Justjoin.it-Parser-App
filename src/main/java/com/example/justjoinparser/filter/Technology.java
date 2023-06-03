package com.example.justjoinparser.filter;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum Technology {

    JAVA("java"),
    PYTHON("python"),
    JS("javascript"),
    PHP("php");

    @JsonValue
    private final String value;
}
