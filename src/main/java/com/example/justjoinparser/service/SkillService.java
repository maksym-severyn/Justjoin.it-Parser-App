package com.example.justjoinparser.service;

import com.example.justjoinparser.model.Skill;
import reactor.core.publisher.Mono;

public interface SkillService {
    Mono<Skill> save(Skill skill);
}
