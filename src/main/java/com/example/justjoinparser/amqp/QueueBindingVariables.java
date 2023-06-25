package com.example.justjoinparser.amqp;

import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.javatuples.Triplet;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class QueueBindingVariables {

    /**
     * Set contains queues to be declare.
     */
    public static final Set<String> QUEUES = Set.of(
        "offers.all",
        "offers.javascript",
        "offers.php",
        "offers.java",
        "offers.python",
        "offers.torun",
        "offers.olsztyn",
        "offers.bydgoszcz",
        "offers.lublin",
        "offers.warszawa",
        "offers.trojmiasto",
        "offers.kielce",
        "offers.poznan",
        "offers.wroclaw",
        "offers.bialystok",
        "offers.czestochowa",
        "offers.bielsko-biala",
        "offers.szczecin",
        "offers.slask",
        "offers.zielona_gora",
        "offers.krakow",
        "offers.rzeszow",
        "offers.lodz",
        "offers.opole"
    );

    /**
     * Map contains exchanges, where key is an exchange name and value is an exchange type.
     */
    public static final Map<String, ExchangeType> EXCHANGES = Map.of(
        "offers.exchange", ExchangeType.TOPIC
    );

    /**
     * List contains bindings, where first value is an exchange name, second is a queue name and third is a routing key
     */
    public static final List<Triplet<String, String, String>> BINDINGS = List.of(
        Triplet.with("offers.exchange", "offers.all", "offers.*.all"),
        Triplet.with("offers.exchange", "offers.all", "offers.all.*"),
        Triplet.with("offers.exchange", "offers.java", "offers.*.java"),
        Triplet.with("offers.exchange", "offers.javascript", "offers.*.javascript"),
        Triplet.with("offers.exchange", "offers.php", "offers.*.php"),
        Triplet.with("offers.exchange", "offers.python", "offers.*.python"),
        Triplet.with("offers.exchange", "offers.bialystok", "offers.bialystok.*"),
        Triplet.with("offers.exchange", "offers.bielsko-biala", "offers.bielsko-biala.*"),
        Triplet.with("offers.exchange", "offers.bydgoszcz", "offers.bydgoszcz.*"),
        Triplet.with("offers.exchange", "offers.czestochowa", "offers.czestochowa.*"),
        Triplet.with("offers.exchange", "offers.kielce", "offers.kielce.*"),
        Triplet.with("offers.exchange", "offers.krakow", "offers.krakow.*"),
        Triplet.with("offers.exchange", "offers.lodz", "offers.lodz.*"),
        Triplet.with("offers.exchange", "offers.lublin", "offers.lublin.*"),
        Triplet.with("offers.exchange", "offers.olsztyn", "offers.olsztyn.*"),
        Triplet.with("offers.exchange", "offers.opole", "offers.opole.*"),
        Triplet.with("offers.exchange", "offers.poznan", "offers.poznan.*"),
        Triplet.with("offers.exchange", "offers.rzeszow", "offers.rzeszow.*"),
        Triplet.with("offers.exchange", "offers.slask", "offers.slask.*"),
        Triplet.with("offers.exchange", "offers.szczecin", "offers.szczecin.*"),
        Triplet.with("offers.exchange", "offers.torun", "offers.torun.*"),
        Triplet.with("offers.exchange", "offers.trojmiasto", "offers.trojmiasto.*"),
        Triplet.with("offers.exchange", "offers.warszawa", "offers.warszawa.*"),
        Triplet.with("offers.exchange", "offers.wroclaw", "offers.wroclaw.*"),
        Triplet.with("offers.exchange", "offers.zielona_gora", "offers.zielona_gora.*")
    );
}
