package com.example.justjoinparser.converter;

import com.example.justjoinparser.converter.generic.TwoWayConverter;
import com.example.justjoinparser.dto.OfferLinkDto;
import com.example.justjoinparser.model.OfferLink;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface OfferLinkDtoToOfferLinkConverter extends TwoWayConverter<OfferLinkDto, OfferLink> {
}
