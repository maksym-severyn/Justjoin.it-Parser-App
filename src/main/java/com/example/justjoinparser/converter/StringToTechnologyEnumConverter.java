package com.example.justjoinparser.converter;

import com.example.justjoinparser.filter.Technology;
import org.springframework.core.convert.converter.Converter;

public class StringToTechnologyEnumConverter implements Converter<String, Technology> {

    @Override
    public Technology convert(String source) {
        return Technology.getFromValueFto(source);
    }
}
