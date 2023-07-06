package com.example.justjoinparser.filter;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum PositionLevel {

    ALL("", "all"),
    JUNIOR("junior", "junior"),
    MID("mid", "mid"),
    SENIOR("senior", "senior");

    private final String filterValue;
    @JsonValue
    private final String valueFto;

    public static PositionLevel getFromValueFto(String value) {
        for (PositionLevel positionLevel : PositionLevel.values()) {
            if (positionLevel.getValueFto().equals(value)) {
                return positionLevel;
            }
        }
        throw new IllegalArgumentException("Cannot parse %s into PositionLevel".formatted(value));
    }
}
