package com.example.justjoinparser.repo;

import com.example.justjoinparser.model.OfferLink;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface OfferLinkRepository extends ReactiveMongoRepository<OfferLink, String> {

}
