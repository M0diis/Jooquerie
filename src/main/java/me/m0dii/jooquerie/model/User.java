package me.m0dii.jooquerie.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@Getter
@AllArgsConstructor
@ToString
public class User {
    private final Long id;
    private final String username;
    private final String email;
}