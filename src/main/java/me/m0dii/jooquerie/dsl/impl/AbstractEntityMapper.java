package me.m0dii.jooquerie.dsl.impl;

import me.m0dii.jooquerie.dsl.EntityMapper;
import org.jooq.Record;
import org.jooq.*;

import java.util.Map;
import java.util.function.Function;

public abstract class AbstractEntityMapper<T, R extends Record> implements EntityMapper<T> {
    private final Table<R> table;
    private final Field<?> idField;
    private final Map<Field<?>, Function<T, Object>> fieldGetters;
    private final java.util.function.Function<Record, T> recordMapper;

    protected AbstractEntityMapper(Table<R> table,
                                Field<?> idField,
                                Map<Field<?>, Function<T, Object>> fieldGetters,
                                java.util.function.Function<Record, T> recordMapper) {
        this.table = table;
        this.idField = idField;
        this.fieldGetters = fieldGetters;
        this.recordMapper = recordMapper;
    }

    @Override
    public Table<?> getTable() {
        return table;
    }

    @Override
    public Field<?> getIdField() {
        return idField;
    }

    @Override
    public T fromRecord(Record rec) {
        return recordMapper.apply(rec);
    }

    @Override
    public void insert(DSLContext dslContext, T entity) {
        InsertSetStep<?> insert = dslContext.insertInto(table);
        InsertSetMoreStep<?> step = null;

        for (Map.Entry<Field<?>, Function<T, Object>> entry : fieldGetters.entrySet()) {
            Field<?> field = entry.getKey();
            Object value = entry.getValue().apply(entity);

            if (step == null) {
                step = ((InsertSetStep) insert).set(field, value);
            } else {
                step = ((InsertSetMoreStep) step).set(field, value);
            }
        }

        if (step != null) {
            step.execute();
        }
    }

    @Override
    public void update(DSLContext dslContext, T entity) {
        UpdateSetStep<?> update = dslContext.update(table);
        UpdateSetMoreStep<?> step = null;
        Object id = null;

        for (Map.Entry<Field<?>, Function<T, Object>> entry : fieldGetters.entrySet()) {
            Field<?> field = entry.getKey();
            Object value = entry.getValue().apply(entity);

            if (field.equals(idField)) {
                id = value;
                continue;
            }

            if (step == null) {
                step = ((UpdateSetStep) update).set(field, value);
            } else {
                step = ((UpdateSetMoreStep) step).set(field, value);
            }
        }

        if (step != null && id != null) {
            step.where(((Field) idField).eq(id)).execute();
        }
    }

    @Override
    public void delete(DSLContext dslContext, T entity) {
        Object id = getIdValue(entity);
        dslContext.delete(table)
                .where(((Field) idField).eq(id))
                .execute();
    }

    private Object getIdValue(T entity) {
        for (Map.Entry<Field<?>, Function<T, Object>> entry : fieldGetters.entrySet()) {
            if (entry.getKey().equals(idField)) {
                return entry.getValue().apply(entity);
            }
        }
        throw new IllegalStateException("No ID field value found for entity");
    }
}