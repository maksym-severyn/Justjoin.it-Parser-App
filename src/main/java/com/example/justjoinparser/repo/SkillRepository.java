package com.example.justjoinparser.repo;

import com.example.justjoinparser.model.Skill;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface SkillRepository extends ReactiveMongoRepository<Skill, String> {
}
