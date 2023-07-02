package com.example.justjoinparser.service;

import com.example.justjoinparser.filter.City;
import com.example.justjoinparser.filter.PositionLevel;
import com.example.justjoinparser.filter.Technology;

public interface OfferSendService {

    void parseAndSend(PositionLevel positionLevel, City city, Technology technology);
}
