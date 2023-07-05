package com.example.justjoinparser.converter;

import com.example.justjoinparser.filter.City;
import org.springframework.core.convert.converter.Converter;

public class StringToCityEnumConverter implements Converter<String, City> {

    @Override
    public City convert(String source) {
        return City.getFromValueFto(source);
    }
}
