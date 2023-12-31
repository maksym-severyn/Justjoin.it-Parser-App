package com.example.justjoinparser.converter;

import com.example.justjoinparser.converter.generic.TwoWayConverter;
import com.example.justjoinparser.dto.OfferDto;
import com.example.justjoinparser.model.Offer;
import org.mapstruct.Mapper;

@Mapper(uses = SkillDtoToSkillConverter.class)
public interface OfferDtoToOfferConverter extends TwoWayConverter<OfferDto, Offer> {
}
