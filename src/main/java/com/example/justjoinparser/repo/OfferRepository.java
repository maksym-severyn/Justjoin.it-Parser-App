package com.example.justjoinparser.repo;

import com.example.justjoinparser.model.Offer;
import com.example.justjoinparser.model.Skill;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;

public interface OfferRepository extends ReactiveMongoRepository<Offer, String> {

//    @Query(value="{city:'?0'}", fields="{'seniority' : 1, 'city' : 1}")
//    Flux<Offer> findAll(String city);

    @Query(value = "{ '_id': ?0 }", fields = "{ 'skills': 1 }")
    Flux<Skill> findSkillsById(String offerId);
}
