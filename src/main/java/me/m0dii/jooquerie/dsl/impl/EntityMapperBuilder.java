package me.m0dii.jooquerie.dsl.impl;

import org.jooq.Field;
import org.jooq.Record;
import org.jooq.Table;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class EntityMapperBuilder<T, R extends Record> {
    private final Table<R> table;
    private Field<?> idField;
    private final Map<Field<?>, Function<T, Object>> fieldGetters = new HashMap<>();
    private Function<Record, T> recordMapper;

    public EntityMapperBuilder(Table<R> table) {
        this.table = table;
    }

    public EntityMapperBuilder<T, R> withId(Field<?> idField, Function<T, Object> getter) {
        this.idField = idField;
        this.fieldGetters.put(idField, getter);
        return this;
    }

    public EntityMapperBuilder<T, R> withField(Field<?> field, Function<T, Object> getter) {
        this.fieldGetters.put(field, getter);
        return this;
    }

    public EntityMapperBuilder<T, R> withRecordMapper(Function<Record, T> recordMapper) {
        this.recordMapper = recordMapper;
        return this;
    }

    public AbstractEntityMapper<T, R> build() {
        if (idField == null || recordMapper == null) {
            throw new IllegalStateException("ID field and record mapper must be set");
        }

        return new AbstractEntityMapper<>(table, idField, fieldGetters, recordMapper) {};
    }
}
