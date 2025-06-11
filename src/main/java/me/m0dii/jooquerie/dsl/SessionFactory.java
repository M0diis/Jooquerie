package me.m0dii.jooquerie.dsl;

import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

public class SessionFactory {
    private final DataSource dataSource;
    private final Map<Class<?>, EntityMapper<?>> mappers = new HashMap<>();

    public SessionFactory(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public <T> void registerEntityMapper(Class<T> entityClass, EntityMapper<T> mapper) {
        mappers.put(entityClass, mapper);
    }

    public EntityManager createEntityManager(SQLDialect dialect) {
        DSLContext dslContext = DSL.using(dataSource, dialect);
        return new EntityManager(dslContext, mappers);
    }
}