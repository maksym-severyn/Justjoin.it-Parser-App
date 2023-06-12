package com.example.justjoinparser.converter.generic;

public interface TwoWayConverter <S, T> extends ObjectConverter<S, T> {

    S convertFrom(T source);
}
