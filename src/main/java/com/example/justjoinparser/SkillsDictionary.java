package com.example.justjoinparser;

import java.util.List;
import java.util.Map;

public class SkillsDictionary {

    public static final Map<String, List<String>> DICTIONARY = Map.ofEntries(
        Map.entry("REST", List.of("rest")),
        Map.entry("Spring", List.of("spring boot", "spring framework")),
        Map.entry("CI/CD", List.of("ci", "ci/cd (jenkins);")),
        Map.entry("Java", List.of("java 8+", "java 8", "java 11", "Java11")),
        Map.entry("WebServices", List.of("webservice", "webservices")),
        Map.entry("Microservices", List.of("microservice")),
        Map.entry("Angular", List.of("angular")),
        Map.entry("HTML", List.of("html")),
        Map.entry("Hibernate", List.of("hibernate", "jpa")),
        Map.entry("AWS", List.of("amazon", "aws")),
        Map.entry("SQL", List.of("database", "bazy danych", "sql server")),
        Map.entry("Git", List.of("github", "gitlab", "git lab", "git/bitbucket")),
        Map.entry("NoSQL", List.of("mongodb")),
        Map.entry("PostgreSQL", List.of("postresql", "postgre sql"))
    );
}
