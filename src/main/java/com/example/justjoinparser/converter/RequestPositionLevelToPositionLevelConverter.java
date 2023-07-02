package com.example.justjoinparser.converter;

import com.example.justjoinparser.converter.generic.ObjectConverter;
import com.example.justjoinparser.filter.PositionLevel;
import com.example.justjoinparser.fto.RequestPositionLevel;
import org.mapstruct.Mapper;

@Mapper
public interface RequestPositionLevelToPositionLevelConverter
    extends ObjectConverter<RequestPositionLevel, PositionLevel> {

    @Override
    PositionLevel convertTo(RequestPositionLevel source);
}
