package com.example.justjoinparser.filter;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum City {

    ALL("all-locations", "all"),
    WARSAW("warszawa", "warsaw"),
    KRAKOW("krakow", "krakow"),
    WROCLAW("wroclaw", "wroclaw"),
    POZNAN("poznan", "poznan"),
    TRICITY("trojmiasto", "tricity"),
    SILESIA("slask", "silesia"),
    BIALYSTOK("bialystok", "bialystok"),
    BIELSKO_BIALA("bielsko-biala", "bielsko_biala"),
    BYDGOSZCZ("bydgoszcz", "bydgoszcz"),
    CZESTOCHOWA("czestochowa", "czestochowa"),
    KIELCE("kielce", "kielce"),
    LUBLIN("lublin", "lublin"),
    LODZ("lodz", "lodz"),
    OLSZTYN("olsztyn", "olsztyn"),
    OPOLE("opole", "opole"),
    RZESZOW("rzeszow", "rzeszow"),
    SZCZECIN("szczecin", "szczecin"),
    TORUN("torun", "torun"),
    ZIELONA_GORA("zielona_gora", "zielona_gora");

    private final String filterValue;
    @JsonValue
    private final String valueFto;

    public static City getFromValueFto(String value) {
        for (City city : City.values()) {
            if (city.getValueFto().equals(value)) {
                return city;
            }
        }
        throw new IllegalArgumentException("Cannot parse %s into City".formatted(value));
    }
}
