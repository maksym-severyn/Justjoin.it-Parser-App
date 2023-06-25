package com.example.justjoinparser.filter;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum Technology {

    JAVA("java"),
    ALL("all"),
    PYTHON("python"),
    JS("javascript"),
    PHP("php");

    @JsonValue
    private final String value;

    public static Technology getFromValue(String value) {
        for (Technology technology: Technology.values()) {
            if (technology.getValue().equals(value)) {
                return technology;
            }
        }
        throw new IllegalArgumentException("Cannot parse %s into Technology".formatted(value));
    }
}
