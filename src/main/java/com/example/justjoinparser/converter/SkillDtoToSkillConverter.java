package com.example.justjoinparser.converter;

import com.example.justjoinparser.converter.generic.TwoWayConverter;
import com.example.justjoinparser.dto.SkillDto;
import com.example.justjoinparser.model.Skill;
import org.mapstruct.Mapper;

@Mapper
public interface SkillDtoToSkillConverter extends TwoWayConverter<SkillDto, Skill> {

    @Override
    Skill convertTo(SkillDto source);

    @Override
    SkillDto convertFrom(Skill source);
}
