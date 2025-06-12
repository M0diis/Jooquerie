package me.m0dii.jooquerie.example;

import me.m0dii.jooquerie.dsl.impl.AbstractEntityMapper;
import me.m0dii.jooquerie.example.generated.records.UsersRecord;
import me.m0dii.jooquerie.example.generated.tables.Users;
import me.m0dii.jooquerie.model.User;

import java.util.Map;

public class UserMapper extends AbstractEntityMapper<User, UsersRecord> {
    public UserMapper() {
        super(Users.USERS, Users.USERS.ID,
                Map.of(
                        Users.USERS.ID, User::getId,
                        Users.USERS.USERNAME, User::getUsername,
                        Users.USERS.EMAIL, User::getEmail
                ), userRecord -> new User(
                        userRecord.get(Users.USERS.ID, Long.class),
                        userRecord.get(Users.USERS.USERNAME, String.class),
                        userRecord.get(Users.USERS.EMAIL, String.class)
                )
        );
    }
}
