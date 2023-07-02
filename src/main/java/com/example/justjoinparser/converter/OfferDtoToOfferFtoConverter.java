package com.example.justjoinparser.converter;

import com.example.justjoinparser.converter.generic.ObjectConverter;
import com.example.justjoinparser.dto.OfferDto;
import com.example.justjoinparser.fto.OfferFto;
import org.mapstruct.Mapper;

@Mapper(uses = SkillDtoToSkillConverter.class)
public interface OfferDtoToOfferFtoConverter extends ObjectConverter<OfferDto, OfferFto> {

    @Override
    OfferFto convertTo(OfferDto source);
}
