package me.m0dii.jooquerie.example;

import me.m0dii.jooquerie.dsl.EntityManager;
import me.m0dii.jooquerie.dsl.SessionFactory;
import me.m0dii.jooquerie.dsl.repository.Repository;
import me.m0dii.jooquerie.dsl.repository.RepositoryProxyFactory;
import me.m0dii.jooquerie.model.User;
import org.h2.jdbcx.JdbcDataSource;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;

import java.util.List;

public class RepositoryExample {

    public interface UserRepository extends Repository<User, Long> {
        List<User> findByUsername(String username);

        List<User> findByEmail(String email);

        List<User> findAll();

        boolean existsByUsername(String username);

        int countByEmail(String email);
    }

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

        EntityManager entityManager = sessionFactory.createEntityManager(SQLDialect.H2);

        User testUser = new User(1L, "john_doe", "john@gmail.com");
        User testUser2 = new User(2L, "admin", "admin@example.com");
        entityManager.persist(testUser);

        UserRepository userRepo = RepositoryProxyFactory.createRepository(entityManager, UserRepository.class);
        userRepo.save(testUser2);

        User user = userRepo.findById(1L);
        List<User> allUsers = userRepo.findAll();
        List<User> johnDoe = userRepo.findByUsername("john_doe");
        List<User> usersWithGmail = userRepo.findByEmail("gmail.com");
        boolean exists = userRepo.existsByUsername("admin");
        int count = userRepo.countByEmail("admin@example.com");

        System.out.println("Found user: " + user);
        System.out.println("User by username: " + johnDoe);
        System.out.println("Users with Gmail: " + usersWithGmail);
        System.out.println("Exists by username: " + exists);
        System.out.println("Count by email: " + count);
    }
}