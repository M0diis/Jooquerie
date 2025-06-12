package me.m0dii.jooquerie.example;

import me.m0dii.jooquerie.dsl.EntityManager;
import me.m0dii.jooquerie.dsl.SessionFactory;
import me.m0dii.jooquerie.model.User;
import org.h2.jdbcx.JdbcDataSource;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;

import java.util.List;


public class JooquerieExample {
    public static void main(String[] args) {
        JdbcDataSource h2DataSource = new JdbcDataSource();
        h2DataSource.setURL("jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1");
        h2DataSource.setUser("sa");
        h2DataSource.setPassword("");

        DSLContext context = DSL.using(h2DataSource, SQLDialect.H2);
        context.execute("CREATE TABLE IF NOT EXISTS USERS (ID BIGINT PRIMARY KEY, USERNAME VARCHAR(100), EMAIL VARCHAR(100))");

        SessionFactory sessionFactory = new SessionFactory(h2DataSource);

        UserMapper userMapper = new UserMapper();
        sessionFactory.registerEntityMapper(User.class, userMapper);

        // Or using EntityMapperBuilder directly:
        // sessionFactory.registerEntityMapper(User.class,
        //         new EntityMapperBuilder<User, UsersRecord>(Users.USERS)
        //                 .withId(Users.USERS.ID, User::getId)
        //                 .withField(Users.USERS.USERNAME, User::getUsername)
        //                 .withField(Users.USERS.EMAIL, User::getEmail)
        //                 .withRecordMapper(record -> new User(
        //                         record.get(Users.USERS.ID, Long.class),
        //                         record.get(Users.USERS.USERNAME, String.class),
        //                         record.get(Users.USERS.EMAIL, String.class)
        //                 ))
        //                 .build());

        EntityManager em = sessionFactory.createEntityManager(SQLDialect.H2);

        User user = new User(1L, "test_user", "test@example.com");
        em.persist(user);

        User user2 = new User(2L, "another_user", "another@example.com");
        em.persist(user2);

        User found = em.find(User.class, 1L);
        System.out.println("Found user: " + found);

        List<User> allUsers = em.findAll(User.class);

        System.out.println("All users:");
        for (User u : allUsers) {
            System.out.println(u);
        }

        em.remove(user2);

        List<User> remainingUsers = em.findAll(User.class);
        System.out.println("Remaining users after deletion:");
        for (User u : remainingUsers) {
            System.out.println(u);
        }
    }
}