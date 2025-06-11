package me.m0dii.jooquerie.dsl;

import lombok.Getter;
import me.m0dii.jooquerie.dsl.query.QueryBuilder;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Record;

import java.util.List;
import java.util.Map;

public class EntityManager {
    private final DSLContext dslContext;
    @Getter
    private final Map<Class<?>, EntityMapper<?>> mappers;

    public EntityManager(DSLContext dslContext, Map<Class<?>, EntityMapper<?>> mappers) {
        this.dslContext = dslContext;
        this.mappers = mappers;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public <T> T find(Class<T> entityClass, Object id) {
        EntityMapper<T> mapper = getMapper(entityClass);

        Record rec = dslContext.selectFrom(mapper.getTable())
                .where(((Field) mapper.getIdField()).eq(id))
                .fetchOne();

        return rec != null ? mapper.fromRecord(rec) : null;
    }

    public <T> List<T> findAll(Class<T> entityClass) {
        EntityMapper<T> mapper = getMapper(entityClass);
        return dslContext.selectFrom(mapper.getTable())
                .fetch()
                .map(mapper::fromRecord);
    }

    public <T> void persist(T entity) {
        EntityMapper<T> mapper = getMapperForEntity(entity);
        mapper.insert(dslContext, entity);
    }

    public <T> T merge(T entity) {
        EntityMapper<T> mapper = getMapperForEntity(entity);
        mapper.update(dslContext, entity);
        return entity;
    }

    public <T> void remove(T entity) {
        EntityMapper<T> mapper = getMapperForEntity(entity);
        mapper.delete(dslContext, entity);
    }

    @SuppressWarnings("unchecked")
    private <T> EntityMapper<T> getMapper(Class<T> entityClass) {
        return (EntityMapper<T>) mappers.get(entityClass);
    }

    @SuppressWarnings("unchecked")
    private <T> EntityMapper<T> getMapperForEntity(T entity) {
        return (EntityMapper<T>) mappers.get(entity.getClass());
    }

    public <T> QueryBuilder<T> createQuery(Class<T> entityClass) {
        EntityMapper<T> mapper = getMapper(entityClass);
        return new QueryBuilder<>(this, dslContext, entityClass, mapper);
    }
}