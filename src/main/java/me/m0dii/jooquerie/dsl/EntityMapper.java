package me.m0dii.jooquerie.dsl;

import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.Table;

public interface EntityMapper<T> {
    Table<?> getTable();
    Field<?> getIdField();
    T fromRecord(Record rec);

    void insert(DSLContext dslContext, T entity);
    void update(DSLContext dslContext, T entity);
    void delete(DSLContext dslContext, T entity);
}