package com.example.justjoinparser.converter;

import com.example.justjoinparser.filter.PositionLevel;
import org.springframework.core.convert.converter.Converter;

public class StringToRequestPositionLevelEnumConverter implements Converter<String, PositionLevel> {

    @Override
    public PositionLevel convert(String source) {
        return PositionLevel.getFromValueFto(source);
    }
}
