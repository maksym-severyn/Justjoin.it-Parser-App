package com.example.justjoinparser.converter;

import com.example.justjoinparser.converter.generic.ObjectConverter;
import com.example.justjoinparser.fto.TopSkillFto;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper
public interface MapSkillToSortedListSkillConverter extends ObjectConverter<Map<String, Long>, List<TopSkillFto>> {

    @Override
    default List<TopSkillFto> convertTo(Map<String, Long> mapSkill) {
        return mapSkill.entrySet().stream()
            .map(this::mapEntryToTopSkillFto)
            .sorted(Comparator.comparingLong(TopSkillFto::frequencyOfAppearance).reversed())
            .toList();
    }

    @Mapping(target = "skillName", source = "key")
    @Mapping(target = "frequencyOfAppearance", source = "value")
    TopSkillFto mapEntryToTopSkillFto(Map.Entry<String, Long> entry);
}
