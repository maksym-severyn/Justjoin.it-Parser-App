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
}
