package com.example.justjoinparser.filter;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum City {

    ALL("all"),
    WARSAW("warszawa"),
    KRAKOW("krakow"),
    WROCLAW("wroclaw"),
    POZNAN("poznan"),
    TRICITY("trojmiasto"),
    SILESIA("slask"),
    BIALYSTOK("bialystok"),
    BIELSKO_BIALA("bielsko-biala"),
    BYDGOSZCZ("bydgoszcz"),
    CZESTOCHOWA("czestochowa"),
    KIELCE("kielce"),
    LUBLIN("lublin"),
    LODZ("lodz"),
    OLSZTYN("olsztyn"),
    OPOLE("opole"),
    RZESZOW("rzeszow"),
    SZCZECIN("szczecin"),
    TORUN("torun"),
    ZIELONA_GORA("zielona_gora");

    @JsonValue
    private final String value;

    public static City getFromValue(String value) {
        for (City city: City.values()) {
            if (city.getValue().equals(value)) {
                return city;
            }
        }
        throw new IllegalArgumentException("Cannot parse %s into City".formatted(value));
    }
}
