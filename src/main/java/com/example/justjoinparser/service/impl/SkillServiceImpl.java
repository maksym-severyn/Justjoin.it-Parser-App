package com.example.justjoinparser.service.impl;

import com.example.justjoinparser.model.Skill;
import com.example.justjoinparser.repo.SkillRepository;
import com.example.justjoinparser.service.SkillService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
class SkillServiceImpl implements SkillService {

    private final SkillRepository skillRepository;

    @Override
    public Mono<Skill> save(@NonNull Skill skill) {
        return skillRepository.save(skill);
    }
}
