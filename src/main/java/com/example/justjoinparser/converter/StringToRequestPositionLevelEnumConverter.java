package com.example.justjoinparser.converter;

import com.example.justjoinparser.fto.RequestPositionLevel;
import org.springframework.core.convert.converter.Converter;

public class StringToRequestPositionLevelEnumConverter implements Converter<String, RequestPositionLevel> {

    @Override
    public RequestPositionLevel convert(String source) {
        return RequestPositionLevel.getFromValue(source);
    }
}
