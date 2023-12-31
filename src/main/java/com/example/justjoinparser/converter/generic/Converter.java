package com.example.justjoinparser.converter.generic;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

public interface Converter<S, T> extends ObjectConverter<S, T> {

    default List<T> convertAllTo(List<S> sourceList) {
        if (Objects.isNull(sourceList)) {
            return Collections.emptyList();
        }
        return sourceList.stream().map(this::convertTo).toList();
    }
}
