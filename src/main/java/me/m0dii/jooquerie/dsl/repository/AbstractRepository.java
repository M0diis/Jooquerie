package me.m0dii.jooquerie.dsl.repository;

import me.m0dii.jooquerie.dsl.EntityManager;
import lombok.Getter;

public abstract class AbstractRepository<T, ID> implements Repository<T, ID> {
    @Getter
    private final EntityManager entityManager;
    private final Class<T> entityClass;

    protected AbstractRepository(EntityManager entityManager, Class<T> entityClass) {
        this.entityManager = entityManager;
        this.entityClass = entityClass;
    }

    @Override
    public T findById(ID id) {
        return entityManager.find(entityClass, id);
    }

    @Override
    public void save(T entity) {
        entityManager.persist(entity);
    }

    @Override
    public void delete(T entity) {
        entityManager.remove(entity);
    }
}