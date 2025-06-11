package me.m0dii.jooquerie.dsl.repository;

import me.m0dii.jooquerie.dsl.EntityManager;

public class RepositoryFactory {
    private final EntityManager entityManager;

    public RepositoryFactory(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public <T, ID> Repository<T, ID> createRepository(Class<T> entityClass) {
        return new AbstractRepository<T, ID>(entityManager, entityClass) {};
    }
}