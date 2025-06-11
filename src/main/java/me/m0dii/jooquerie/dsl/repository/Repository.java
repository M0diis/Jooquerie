package me.m0dii.jooquerie.dsl.repository;

import me.m0dii.jooquerie.dsl.EntityManager;

public interface Repository<T, ID> {
    T findById(ID id);
    void save(T entity);
    void delete(T entity);

    EntityManager getEntityManager();
}