package com.example.justjoinparser.model;

import com.example.justjoinparser.filter.Technology;
import lombok.*;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@Document
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Skill {

    @Id
    @EqualsAndHashCode.Include
    private String id;
    private String seniority;
    private String city;
    private Technology technology;
    private String name;
    private String level;
    private String offer;
}