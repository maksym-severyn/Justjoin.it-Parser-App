package com.example.justjoinparser.converter.generic;

public interface ObjectConverter<S, T> {

    T convertTo(S source);
}
