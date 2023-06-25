package com.example.justjoinparser;

import com.example.justjoinparser.filter.City;
import com.example.justjoinparser.filter.Technology;
import java.util.HashMap;
import java.util.Map;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.javatuples.Pair;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class QueueBindingConfig {

    public static final Map<String, Pair<String, String>> QUEUE_BINDING = init();

    private static Map<String, Pair<String, String>> init() {
        Map<String, Pair<String, String>> map = new HashMap<>();
        for (City city : City.values()) {
            map.put(
                "offers." + city.getValue() + ".*",
                Pair.with("offers.exchange", "offers." + city.getValue())
            );
        }
        for (Technology technology : Technology.values()) {
            map.put(
                "offers.*." + technology.getValue(),
                Pair.with("offers.exchange", "offers." + technology.getValue())
            );
        }
        return map;
    }
}
