package com.example.justjoinparser.repo;

import com.example.justjoinparser.Skill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PageRepo extends JpaRepository<Skill, Long> {
}
