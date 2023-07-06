package com.example.justjoinparser.service.impl;

import com.example.justjoinparser.converter.OfferDtoToOfferConverter;
import com.example.justjoinparser.dto.OfferDto;
import com.example.justjoinparser.filter.City;
import com.example.justjoinparser.filter.PositionLevel;
import com.example.justjoinparser.filter.Technology;
import com.example.justjoinparser.model.Skill;
import com.example.justjoinparser.repo.OfferRepository;
import com.example.justjoinparser.service.OfferService;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
class OfferServiceImpl implements OfferService {

    private final OfferDtoToOfferConverter converter;
    private final OfferRepository repository;

    @Override
    public Mono<OfferDto> save(OfferDto offerDtoToSave) {
        return repository.save(converter.convertTo(offerDtoToSave))
            .flatMap(savedOffer -> Mono.just(converter.convertFrom(savedOffer))
                .doOnNext(saved -> log.trace("Saved offerDto with id: {}", saved.id()))
            );
    }

    @Override
    public Mono<Long> count() {
        return repository.count();
    }

    @Override
    public Mono<Map<String, Long>> findSkillsByParameters(PositionLevel positionLevel, City city,
                                                          Technology technology) {
        return repository.findSkillsByParameters(technology, city, positionLevel)
            .flatMap(offer -> Flux.fromIterable(offer.getSkills()))
            .collectList()
            .map(this::sortSkillsByHighestDemand);
    }

    @Override
    public Mono<Map<String, Long>> findTopSkillsByParameters(Long top, PositionLevel positionLevel, City city,
                                                             Technology technology) {
        return this.findSkillsByParameters(positionLevel, city, technology)
            .map(skillsSortedByDemand -> skillsSortedByDemand.entrySet().stream()
                .limit(top)
                .collect(Collectors.toMap(
                    Map.Entry::getKey,
                    Map.Entry::getValue,
                    (e1, e2) -> e1,
                    LinkedHashMap::new)
                )
            );
    }

    private Map<String, Long> sortSkillsByHighestDemand(List<Skill> skills) {
        return skills.stream()
            .collect(Collectors.groupingBy(
                Skill::getName,
                Collectors.counting()))
            .entrySet().stream()
            .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
    }
}
