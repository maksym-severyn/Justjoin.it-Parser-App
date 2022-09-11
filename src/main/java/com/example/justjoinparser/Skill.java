package com.example.justjoinparser;

import lombok.*;

import javax.persistence.*;

@Getter
@Setter
@Entity
@Table(name = "skill")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class Skill {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String seniority;
    private String city;
    private String name;
    private String level;
    private String offer;
}