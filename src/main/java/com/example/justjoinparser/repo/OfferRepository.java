package com.example.justjoinparser.repo;

import com.example.justjoinparser.filter.City;
import com.example.justjoinparser.filter.PositionLevel;
import com.example.justjoinparser.filter.Technology;
import com.example.justjoinparser.model.Offer;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;

public interface OfferRepository extends ReactiveMongoRepository<Offer, String> {

    @Query(value = "{ 'technology': ?0, 'city': ?1, 'seniority': ?2 }", fields = "{ 'skills': 1 }")
    Flux<Offer> findSkillsByParameters(Technology technology, City city, PositionLevel positionLevel);
}
