package com.example.justjoinparser.filter;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum Technology {

    JAVA("java", "java"),
    PYTHON("python", "python"),
    JS("javascript", "java_script"),
    PHP("php", "php");

    private final String filterValue;
    @JsonValue
    private final String valueFto;

    public static Technology getFromValueFto(String value) {
        for (Technology technology : Technology.values()) {
            if (technology.getValueFto().equals(value)) {
                return technology;
            }
        }
        throw new IllegalArgumentException("Cannot parse %s into Technology".formatted(value));
    }
}
