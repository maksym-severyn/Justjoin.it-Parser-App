package com.example.justjoinparser.filter;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum PositionLevel {

    ALL(""),
    JUNIOR("junior"),
    MID("mid"),
    SENIOR("senior");

    private final String value;

    public static PositionLevel getFromValue(String value) {
        for (PositionLevel positionLevel: PositionLevel.values()) {
            if (positionLevel.getValue().equals(value)) {
                return positionLevel;
            }
        }
        throw new IllegalArgumentException("Cannot parse %s into PositionLevel".formatted(value));
    }
}
