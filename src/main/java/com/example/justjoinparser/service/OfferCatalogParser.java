package com.example.justjoinparser.service;

import com.example.justjoinparser.filter.City;
import com.example.justjoinparser.filter.PositionLevel;
import com.example.justjoinparser.filter.Technology;
import java.util.Set;

public interface OfferCatalogParser {

    Set<String> getOffersLinks(Technology technology, City city, PositionLevel positionLevel);
}
