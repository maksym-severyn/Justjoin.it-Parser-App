package com.example.justjoinparser.fto;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum RequestPositionLevel {

    ALL("all"),
    JUNIOR("junior"),
    MID("mid"),
    SENIOR("senior");

    @JsonValue
    private final String value;

    public static RequestPositionLevel getFromValue(String value) {
        for (RequestPositionLevel positionLevel: RequestPositionLevel.values()) {
            if (positionLevel.getValue().equals(value)) {
                return positionLevel;
            }
        }
        throw new IllegalArgumentException("Cannot parse %s into Technology".formatted(value));
    }
}
