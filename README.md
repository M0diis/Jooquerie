# Jooquerie

Jooquerie is a Java library that provides a simplified abstraction layer over jOOQ, making it easier to work with
databases in a type-safe manner.

## Overview

This library addresses common challenges when working with jOOQ by providing:  
Entity mapping framework for converting between database records and domain objects
Simplified CRUD operations
Type-safe query building while reducing boilerplate

## Features

Entity Mappers: Convert between database records and domain objects
Generic CRUD Operations: Common operations like create, read, update, and delete
Type-Safe Queries: Leverage jOOQ's type safety with a simpler API

## Installation

```groovy
dependencies {
    implementation 'me.m0dii:jooquerie:1.0.0'
}
```

## Development

1. Clone the repository:
```bash
git clone https://github.com/M0diis/Jooquerie.git
```

2. Navigate to the project directory:
```bash
cd Jooquerie
```

3. Build the project using Gradle:
```bash
./gradlew build
```

## Usage

### Mapper

```java
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
```

### Repository

Create a repository interface for your entity. This interface extends `Repository<T, ID>` where `T` is your entity type and `ID` is the type of the entity's identifier.
```java
public interface UserRepository extends Repository<User, Long> {
    List<User> findByUsername(String username);
    List<User> findByUsernameAndEmail(String username, String email);
    List<User> findLikeByEmail(String email);
    List<User> findAll();
    
    boolean existsByUsername(String username);
    int countByEmail(String email);
}
```

Create a repository using RepositoryProxyFactory:
```java
UserRepository userRepo = RepositoryProxyFactory.createRepository(entityManager, UserRepository.class);
```

Use the repository to perform CRUD operations:
```java
User testUser = new User(1L, "john_doe", "john@gmail.com");

UserRepository userRepo = RepositoryProxyFactory.createRepository(entityManager, UserRepository.class);
userRepo.save(testUser);

User byId = userRepo.findById(1L);
List<User> allUsers = userRepo.findAll();
List<User> byUserName = userRepo.findByUsername("john_doe");
List<User> byUsernameAndEmail = userRepo.findByUsernameAndEmail("tom", "tom@domain.com");
List<User> likeByEmail = userRepo.findLikeByEmail("gmail.com");
boolean existsByUsername = userRepo.existsByUsername("admin");
int count = userRepo.countByEmail("admin@example.com");
```

Note: you will also need to create a `SessionFactory` and `EntityManager` and a mapper to manage your entities and perform operations on them. 
See the example below for a complete setup.

### Example

Full example can be found in `me.m0dii.jooquerie.example` package.

```java
// Initialize jOOQ DSLContext
DSLContext dslContext = DSL.using(connection, SQLDialect.H2);

// Create SessionFactory
SessionFactory sessionFactory = new SessionFactory(h2DataSource);

sessionFactory.registerEntityMapper(User.class,
        new EntityMapperBuilder<User, UsersRecord>(Users.USERS)
                .withId(Users.USERS.ID, User::getId)
                .withField(Users.USERS.USERNAME, User::getUsername)
                .withField(Users.USERS.EMAIL, User::getEmail)
                .withRecordMapper(record -> new User(
                        record.get(Users.USERS.ID, Long.class),
                        record.get(Users.USERS.USERNAME, String.class),
                        record.get(Users.USERS.EMAIL, String.class)
                ))
                .build());

// Create EntityManager
EntityManager entityManager = sessionFactory.createEntityManager(SQLDialect.H2);

// Create
User user = new User();
user.setUsername("john_doe");
user.setEmail("john@example.com");
entityManager.persist(user);

// Read
User foundUser = entityManager.find(User.class, 1L);

// Update
foundUser.setEmail("john.doe@example.com");
entityManager.merge(foundUser);

// Delete
entityManager.remove(foundUser);
```