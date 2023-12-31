package com.example.justjoinparser.converter;

import com.example.justjoinparser.converter.generic.Converter;
import com.example.justjoinparser.dto.OfferDto;
import com.example.justjoinparser.fto.OfferFto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface OfferDtoToOfferFtoConverter extends Converter<OfferDto, OfferFto> {
}
